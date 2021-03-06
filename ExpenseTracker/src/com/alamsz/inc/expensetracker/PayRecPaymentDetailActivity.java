package com.alamsz.inc.expensetracker;

import java.util.Calendar;
import java.util.Iterator;

import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.alamsz.inc.expensetracker.dao.ConfigurationExpTracker;
import com.alamsz.inc.expensetracker.dao.DatabaseHandler;
import com.alamsz.inc.expensetracker.dao.ExpenseTracker;
import com.alamsz.inc.expensetracker.dao.PayRecMaster;
import com.alamsz.inc.expensetracker.dao.PayRecPayment;
import com.alamsz.inc.expensetracker.fragment.DateDialogFragment;
import com.alamsz.inc.expensetracker.service.ExpenseTrackerService;
import com.alamsz.inc.expensetracker.utility.FormatHelper;
import com.alamsz.inc.expensetracker.utility.StaticVariables;

public class PayRecPaymentDetailActivity extends SherlockFragmentActivity {
	ExpenseTrackerService expenseTrackerService;
	String processMessage;
	EditText dateFieldChosen;
	String mode;
	String transType;
	boolean isReceive;
	Long idPayRec;
	Long id;
	String strDesc;
	String strAmount;
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.pay_rec_payment_detail);
		EditText edDateInput = (EditText) findViewById(R.id.payRecPaymentEdDateInput);
		FormatHelper.setCurrentDateOnView(edDateInput);
		Intent intent = getIntent();
		DatabaseHandler dbHandler = FormatHelper.getDBHandler(ExpenseTrackerActivity.dbHandler,this);
		expenseTrackerService = new ExpenseTrackerService(dbHandler);
		transType = intent.getStringExtra("trans");
		mode = intent.getStringExtra(StaticVariables.MODE);
		idPayRec = intent.getLongExtra(PayRecMaster.ID_MASTER, 0);
		strAmount = intent.getStringExtra(PayRecMaster.AMOUNT);
		strDesc = intent.getStringExtra(PayRecMaster.DESCRIPTION);
			
		TextView title = (TextView) findViewById(R.id.payRecPaymentDetailTitle);
		if(transType.equals(PayRecMaster.RECEIVABLE_CODE)){
			title.setText(getString(R.string.receivable_payment));
			isReceive = true;
		}else{
			title.setText(getString(R.string.payable_payment));
			isReceive = false;
		}
		ArrayAdapter<ConfigurationExpTracker> fundSourceAdapter = new ArrayAdapter<ConfigurationExpTracker>(
				getApplicationContext(),
				R.layout.spinner_item, StaticVariables.listOfFundSource); 
		fundSourceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		Spinner spinTrans = (Spinner) findViewById(R.id.expenseFundSourceSpinner);
		spinTrans.setAdapter(fundSourceAdapter);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if(mode.equals(StaticVariables.MODE_MOD)){
			Long transId = intent.getLongExtra(PayRecMaster.ID, 0);
			PayRecPayment paymentTemp = expenseTrackerService.findByPKPayRecDetail(transId);
			id = paymentTemp.getId();
			Button btnProcess = (Button) findViewById(R.id.payRecPaymentBtnAddorUpdpayRecPayment);
			btnProcess.setText(getString(R.string.btn_delete));
			if(paymentTemp != null){
				edDateInput = (EditText) findViewById(R.id.payRecPaymentEdDateInput);
				EditText edDescription = (EditText) findViewById(R.id.payRecPaymentEdDescription);
				EditText edAmount = (EditText) findViewById(R.id.payRecPaymentEdAmount);
				CheckBox checkTrans = (CheckBox) findViewById(R.id.payRecPaymentCheckMarkTrans);
				spinTrans = (Spinner) findViewById(R.id.expenseFundSourceSpinner);
				String strDateInput = FormatHelper.formatDateForDisplay(paymentTemp.getDateInput());
				String strDescription = paymentTemp.getDescription();
				String strAmount = String.valueOf(paymentTemp.getAmount());
				long idTrans = paymentTemp.getIdTrans();
				boolean strMarkTrans = idTrans==0?false:true;
				edDateInput.setText(strDateInput);
				edDescription.setText(strDescription);
				edAmount.setText(strAmount);
				checkTrans.setChecked(strMarkTrans);
				checkTrans.setEnabled(false);
				edDateInput.setFocusable(false);
				edAmount.setFocusable(false);
				edDescription.setFocusable(false);
				spinTrans.setFocusable(false);
				//only set when the payment mark as transaction
				if(strMarkTrans){
					ExpenseTracker expTracker = expenseTrackerService.findByPKTransaction(idTrans);
					if(expTracker != null){
						int i = 0;
						for (Iterator iterator = StaticVariables.listOfFundSource.iterator(); iterator
								.hasNext();) {
							ConfigurationExpTracker type = (ConfigurationExpTracker) iterator.next();
							if(type.getTableCode().equals(expTracker.getCategory())){
								spinTrans.setSelection(i);
								break;
							}
							i++;
							
						}
						
					}else{
						spinTrans.setSelection(1);
					}
					
				}
				
				
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		DatabaseHandler dbHandler = FormatHelper.getDBHandler(ExpenseTrackerActivity.dbHandler,this);
		expenseTrackerService = new ExpenseTrackerService(dbHandler);
		
	}

	public void addOrModPayRecPayment(View view){
		boolean validForProcess = true;
		PayRecPayment payRecPayment = new PayRecPayment();
		EditText edDateInput = (EditText) findViewById(R.id.payRecPaymentEdDateInput);
		EditText edDescription = (EditText) findViewById(R.id.payRecPaymentEdDescription);
		EditText edAmount = (EditText) findViewById(R.id.payRecPaymentEdAmount);
		CheckBox checkTrans = (CheckBox) findViewById(R.id.payRecPaymentCheckMarkTrans);
		Spinner spinTrans = (Spinner) findViewById(R.id.expenseFundSourceSpinner);
		String strDateInput = edDateInput.getText().toString();
		String strDescription = edDescription.getText().toString();
		String strAmount = edAmount.getText().toString().equals("")?"0":edAmount.getText().toString();
		boolean strMarkTrans = checkTrans.isChecked();
		Button btnProcess = (Button) findViewById(R.id.payRecPaymentBtnAddorUpdpayRecPayment);
		
		if(strDescription.equals("")){
			validForProcess = false;
			String fieldRequired = String.format(getString(R.string.emptyTextValidation),getString(R.string.description));
			edDescription.setError(fieldRequired);
			
		}
		if(strAmount.equals("")){
			String fieldRequired = String.format(getString(R.string.emptyTextValidation),getString(R.string.amount));
			edAmount.setError(fieldRequired);
			validForProcess = false;
		}else if(strAmount.equals("0")){
			String fieldRequired = String.format(getString(R.string.emptyTextValidation),getString(R.string.amount));
			edAmount.setError(fieldRequired);
			validForProcess = false;
		}
		if(strMarkTrans){
			if(spinTrans.getSelectedItem().equals("")){
				validForProcess = false;
			}
		}
		if(validForProcess){
			PayRecPayment payRec = new PayRecPayment();
			payRec.setDateInput(FormatHelper.formatDateToLong(strDateInput));
			payRec.setAmount(Integer.parseInt(strAmount));
			payRec.setMarkAsTrans(strMarkTrans);
			payRec.setDescription(strDescription);
			payRec.setCategory(((ConfigurationExpTracker)spinTrans.getSelectedItem()).getTableCode());
			payRec.setIdMaster(idPayRec);
			payRec.setType(transType);
			
			if(mode.equals(StaticVariables.MODE_ADD)){
				if(strMarkTrans){
					if(isReceive){
						payRec.setTransCategory("REC");
						payRec.setTransType("D");
					}else{
						payRec.setTransCategory("PAY");
						payRec.setTransType("K");
					}
					
				}
				payRec = expenseTrackerService.addPayRecDetailData(payRec);
				processMessage = String.format(
						getString(R.string.add_success), strDescription );
				btnProcess.setText(getString(R.string.btn_delete));
				checkTrans.setChecked(strMarkTrans);
				backToCallerActivity();
			}else{
				payRec.setId(id);
				expenseTrackerService.deletePayRecDetail(payRec);
				processMessage = String.format(
						getString(R.string.del_success), strDescription );
				btnProcess.setText(getString(R.string.btn_add));
				backToCallerActivity();
			}
			
		}else{
			processMessage = getString(R.string.allFieldValidation);
		}
		Toast.makeText(getApplicationContext(), processMessage, Toast.LENGTH_SHORT).show();
		
		
	}
	
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home

			backToCallerActivity();
			break;
		}
		return true;
	}

	private void backToCallerActivity() {
		Intent intent = new Intent(this, PayRecPaymentActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(PayRecMaster.ID_MASTER, idPayRec);
		intent.putExtra(StaticVariables.MODE, StaticVariables.MODE_ADD);
		intent.putExtra(PayRecMaster.AMOUNT, strAmount);
		intent.putExtra(PayRecMaster.DESCRIPTION, strDesc);
		intent.putExtra("trans", transType);
		startActivity(intent);
	}
	
	public void payRecPaymentDateInputClick(View view){
		dateFieldChosen = (EditText) findViewById(R.id.payRecPaymentEdDateInput);
		showDatePicker();
	}
	
	private void showDatePicker() {
		DateDialogFragment date = new DateDialogFragment();
		/**
		 * Set Up Current Date Into dialog
		 */
		// if(dateField.getText().equals("")){
		Calendar calender = Calendar.getInstance();
		Bundle args = new Bundle();
		args.putInt("year", calender.get(Calendar.YEAR));
		args.putInt("month", calender.get(Calendar.MONTH));
		args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));

		date.setArguments(args);
		// }
		/**
		 * Set Call back to capture selected date
		 */
		date.setCallBack(ondate);
		date.show(getSupportFragmentManager(), "Date Picker");
	}

	OnDateSetListener ondate = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			String formattedDay = FormatHelper.formatTwoDigitsDay(day);
			String formattedMonth = FormatHelper.formatTwoDigitsMonth(month);
			dateFieldChosen.setText(new StringBuilder().append(formattedDay)
					.append("-").append(formattedMonth).append("-")
					.append(year).append(" "));
			Log.d(getClass().getName(), formattedDay);
			Log.d(getClass().getName(), formattedMonth);
			Log.d(getClass().getName(), String.valueOf(year));
		}
	};
	
	
}
