package com.mateuyabar.android.pillownfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

import com.mateuyabar.android.pillownfc.util.WriteTagHelper;

import java.util.Date;

/**
 * Sample activity
 */
public class PillowNfcSample extends ActionBarActivity {
	PillowNfcManager nfcManager;
	WriteTagHelper writeHelper;
	String jk;
	String tagRead2;
	int cek=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sample);

		final Random myRandom = new Random(6);
		nfcManager = new PillowNfcManager(this);
		nfcManager.onActivityCreate();
		//onNewIntent(getIntent());

		writeHelper= new WriteTagHelper(this, nfcManager);
		//nfcManager.setOnTagReadListener(writeHelper);
		nfcManager.setOnTagWriteErrorListener(writeHelper);
		nfcManager.setOnTagWriteListener(writeHelper);

		nfcManager.setOnTagReadListener(new PillowNfcManager.TagReadListener() {
			@Override
			public void onTagRead(String tagRead) {

				//Toast.makeText(PillowNfcSample.this, "tag read:"+tagRead, Toast.LENGTH_LONG).show();
				tagRead2 = tagRead;
				EditText tv1 = (EditText) findViewById(R.id.editText_id);
				EditText tv2 = (EditText) findViewById(R.id.editText_nama);
				EditText tv3 = (EditText) findViewById(R.id.editText_alamat);
				EditText tv4 = (EditText) findViewById(R.id.editText_saldoawal);
				RadioButton JKL = (RadioButton) findViewById(R.id.radioButton_L);
				RadioButton JKP = (RadioButton) findViewById(R.id.radioButton_P);
				//Toast.makeText(PillowNfcSample.this, "tag read:"+tagRead, Toast.LENGTH_LONG).show();
				tv1.setText(tagRead.substring(8, 24));
				tv2.setText(tagRead.substring(24, 40));
				if (tagRead.substring(40, 56).contains("Laki-Laki")) {
					JKL.setChecked(true);
				} else {
					JKP.setChecked(true);
				}
				//tv3.setText(tagRead.substring(40, 56));
				tv3.setText(tagRead.substring(56, 72));
				tv4.setText(tagRead.substring(72, 88));

				if (tv1.getText().equals("")) {
					/*idrandom random = new idrandom();
					tv1.setText(random.generateActivationCode(6));*/
				}
				if(cek!=0){
				writeHelper.closeDialog();
					cek = 0;}

			}
		});


		// If don't want to use the Write helper you can use the following code
		/*nfcManager.setOnTagWriteListener(new PillowNfcManager.TagWriteListener() {
			@Override
			public void onTagWritten() {
				//Toast.makeText(PillowNfcSample.this, "tag writen", Toast.LENGTH_LONG).show();
			}
		});
		nfcManager.setOnTagWriteErrorListener(new PillowNfcManager.TagWriteErrorListener() {
			@Override
			public void onTagWriteError(NFCWriteException exception) {
				Toast.makeText(PillowNfcSample.this, exception.getType().toString(), Toast.LENGTH_LONG).show();
			}
		});*/

		Button writeButton = (Button) findViewById(R.id.write_button);
		Button topup = (Button) findViewById(R.id.topup_button);
		Button clear = (Button) findViewById(R.id.clear_button);
		final EditText editText_id = (EditText) findViewById(R.id.editText_id) ;
		final EditText editText_nama = (EditText) findViewById(R.id.editText_nama);
		final RadioButton radioButton_L = (RadioButton) findViewById(R.id.radioButton_L);
		final RadioButton radioButton_P = (RadioButton) findViewById(R.id.radioButton_P);
		final EditText editText_alamat = (EditText) findViewById(R.id.editText_alamat);
		final EditText editText_saldoawal = (EditText) findViewById(R.id.editText_saldoawal);
		final EditText editText_saldo = (EditText) findViewById(R.id.editText_saldo);
		final RadioGroup jeniskelamin = (RadioGroup) findViewById(R.id.radiogrup);
		writeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String id = "        ";//String.format("%1$"+ 16+ "s",editText_id.getText().toString());
				String id2 = String.format("%1$-"+ 16+ "s",editText_id.getText().toString());
				String nama = String.format("%1$-"+ 16+ "s",editText_nama.getText().toString());

				if(radioButton_L.isChecked())
				{
					 jk = String.format("%1$-"+ 16+ "s","Laki-Laki");
				}
				else if(radioButton_P.isChecked())
				{
					 jk = String.format("%1$-"+ 16+ "s","Perempuan");
				}
				String alamat = String.format("%1$-"+ 16+ "s",editText_alamat.getText().toString());
				String saldo = String.format("%1$-"+ 100+ "s",editText_saldoawal.getText().toString());
				String text = id+id2+nama+jk+alamat+saldo;

				writeHelper.writeTextIdent(text, editText_saldo);
//				// If don't want to use the Write helper you can use the following code
				//nfcManager.writeText(text);
				Toast.makeText(PillowNfcSample.this, "Data Sedang Diproses", Toast.LENGTH_LONG).show();
			}
		});

		writeHelper= new WriteTagHelper(this, nfcManager);
		nfcManager.setOnTagWriteErrorListener(writeHelper);
		nfcManager.setOnTagWriteListener(writeHelper);
		// If don't want to use the Write helper you can use the following code
		/*nfcManager.setOnTagWriteListener(new PillowNfcManager.TagWriteListener() {
			@Override
			public void onTagWritten() {
				//Toast.makeText(PillowNfcSample.this, "tag writen", Toast.LENGTH_LONG).show();
			}
		});
		nfcManager.setOnTagWriteErrorListener(new PillowNfcManager.TagWriteErrorListener() {
			@Override
			public void onTagWriteError(NFCWriteException exception) {
				Toast.makeText(PillowNfcSample.this, exception.getType().toString(), Toast.LENGTH_LONG).show();
			}
		});
*/
		topup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int x;
				cek++;
				String saldo = editText_saldoawal.getText().toString();
				if(editText_saldo.getText().toString().isEmpty()){
					Toast.makeText(PillowNfcSample.this, "Saldo Harus diisi", Toast.LENGTH_LONG).show();
				}else{
					String topup = editText_saldo.getText().toString();
					String saldoClean=saldo.replaceAll(" ","");//[^\p{Print}]
					String topupclean = topup.replaceAll(" ","");
					x = Integer.parseInt(saldoClean)+Integer.parseInt(topupclean);
					//tv7.setText(String.valueOf(x));

					//EditText tv7 = (EditText) findViewById(R.id.sisa);
					//String y = String.valueOf(x);
					String bersih = tagRead2.substring(0, 72);
					//bersih = bersih + y;
					//String text = tv2.getText().toString();//new Date().toString();
					String text = bersih + String.format("%1$-"+ 16+ "s",String.valueOf(x));
					//nfcManager.writeText(text);
					writeHelper.writeText(text, editText_saldo,cek);
					//if(cek==1){Toast.makeText(PillowNfcSample.this, "Saldo Sedang Diproses", Toast.LENGTH_LONG).show();}
				}

			}
		});

		clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editText_id.setText("");
				editText_nama.setText("");
				editText_alamat.setText("");
				editText_saldo.setText("");
				editText_saldoawal.setText("");
				jeniskelamin.clearCheck();
				idrandom random = new idrandom();
				editText_id.setText(random.generateActivationCode(10));
			}
		});
		onNewIntent(getIntent()); //Inne
	}

	@Override
	protected void onResume() {
		super.onResume();
		nfcManager.onActivityResume();
	}

	@Override
	protected void onPause() {
		nfcManager.onActivityPause();
		super.onPause();
	}

	@Override
	public void onNewIntent(Intent intent){
		nfcManager.onActivityNewIntent(intent);
	}

}
