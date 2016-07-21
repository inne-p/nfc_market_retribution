package com.mateuyabar.android.pillownfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mateuyabar.android.pillownfc.NFCWriteException.NFCErrorType;
import com.mateuyabar.android.pillownfc.util.SharedProperty;

import java.io.IOException;

//import com.google.common.base.Preconditions;

/**
 * NFC manager.
 *
 *
 */
public class PillowNfcManager {
	NfcAdapter nfcAdapter;
	Activity activity;
	AlertDialog dialog;
	PendingIntent pendingIntent;
	SharedProperty sp;
	int dialogViewId = R.layout.write_nfc_dialog_view;
	TagReadListener onTagReadListener;
	TagWriteListener onTagWriteListener;
	TagWriteErrorListener onTagWriteErrorListener;

	String writeText = null, writeTextIdent = null;


	public PillowNfcManager(Activity activity) {
		sp = new SharedProperty();
		System.out.println("I'm here");
		this.activity = activity;
	}

	/**
	 * Sets the listener to read events
	 */
	public void setOnTagReadListener(TagReadListener onTagReadListener) {
		this.onTagReadListener = onTagReadListener;
	}

	/**
	 * Sets the listener to write events
	 */
	public void setOnTagWriteListener(TagWriteListener onTagWriteListener) {
		this.onTagWriteListener = onTagWriteListener;
	}

	/**
	 * Sets the listener to write error events
	 */
	public void setOnTagWriteErrorListener(TagWriteErrorListener onTagWriteErrorListener) {
		this.onTagWriteErrorListener = onTagWriteErrorListener;
	}

	/**
	 * Indicates that we want to write the given text to the next tag detected
	 */
	public void writeTextIdent(String writeText) {
		this.writeTextIdent=writeText;
	}
	public void writeText(String writeText) {
		this.writeText = writeText;
		// I n n e
		System.out.println("Log:"+writeText);
		if (nfcAdapter != null) {
			if (!nfcAdapter.isEnabled()) {
				//TODO indicate that wireless should be opened
				nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
			}
			nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
		}
		Tag tag = activity.getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Log.i("Log :", activity.getIntent().toString());


		try {
			writeTag(activity, tag, writeText);
			onTagWriteListener.onTagWritten();
		} catch (NFCWriteException exception) {
			onTagWriteErrorListener.onTagWriteError(exception);
		} finally {
			this.writeText = null;
		}
	}

	/**
	 * Stops a writeText operation
	 */
	public void undoWriteText() {
		this.writeText = null;this.writeTextIdent = null;
	}


	/**
	 * To be executed on OnCreate of the activity
	 * @return true if the device has nfc capabilities
	 */
	public boolean onActivityCreate() {
		nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		if(nfcAdapter==null){
			Toast.makeText(activity.getApplicationContext(), "Hp tidak mendukung NFC", Toast.LENGTH_LONG).show();
			LayoutInflater inflater = LayoutInflater.from(activity);
			View view = inflater.inflate(dialogViewId, null, false);
			ImageView image = new ImageView(activity);
			image.setImageResource(R.drawable.ic_nfc_black_48dp);
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle("Perangkat Tidak Mendukung NFC")
					.setView(view)
					.setCancelable(false)
					.setNegativeButton("Keluar", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							System.exit(1);
						}
					});
			dialog=builder.create();
			dialog.show();
			pendingIntent = PendingIntent.getActivity(activity, 0,
					new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			return nfcAdapter==null;

		}else{
			pendingIntent = PendingIntent.getActivity(activity, 0,
					new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			return nfcAdapter!=null;
		}

	}

	/**
	 * To be executed on onResume of the activity
	 */
	public void onActivityResume() {
		if (nfcAdapter != null) {
			if (!nfcAdapter.isEnabled()) {
				//TODO indicate that wireless should be opened
			}
			nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
		}
	}

	/**
	 * To be executed on onPause of the activity
	 */
	public void onActivityPause() {
		if (nfcAdapter != null) {
			nfcAdapter.disableForegroundDispatch(activity);
		}
	}

	/**
	 * To be executed on onNewIntent of activity
	 * @param intent
	 */
	public void onActivityNewIntent(Intent intent) {
		// TODO Check if the following line has any use
		// activity.setIntent(intent);
		System.out.println("Wit:"+writeTextIdent);
		if (writeText == null && writeTextIdent==null){
			System.out.println("I'm over here");
			readTagFromIntent(intent);}
		else {
			System.out.println("I'm over there");
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			System.out.println(sp.isIdEqual(byte2HexString(tag.getId())));
			System.out.println(byte2HexString(tag.getId())==sp.getId());
			System.out.println(byte2HexString(tag.getId()).contains(sp.getId()));
			if(sp.isIdEqual(byte2HexString(tag.getId()))) {
				System.out.println("tag:" + tag);
				try {
					writeTag(activity, tag, writeTextIdent);
					onTagWriteListener.onTagWritten();
				} catch (NFCWriteException exception) {
					onTagWriteErrorListener.onTagWriteError(exception);
				} finally {
					writeText = null;
					writeTextIdent = null;
				}
			}else
				Toast.makeText(activity.getApplicationContext(), "Berbeda", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Reads a tag for a given intent and notifies listeners
	 * @param intent
	 */
	public void readTagFromIntent(Intent intent) {
		String action = intent.getAction();
		System.out.println(action);
		Tag myTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

			//byte[] uid = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
//			String serialNumber = String.format("%02x", uid);
			System.out.println(myTag.getId());
			String id = "ID (";
			id += byte2HexString(myTag.getId());
			id += ")";
			System.out.println(id);
			sp.setId(byte2HexString(myTag.getId()));
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

			if (rawMsgs != null) {
				NdefRecord[] records = ((NdefMessage) rawMsgs[0]).getRecords();
				String text = ndefRecordToString(records[0]);

				System.out.println("Text "+text);
				onTagReadListener.onTagRead(text);
			}
		}
		else if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)){
			System.out.println("I'm hereee !");
			sp.setId(byte2HexString(myTag.getId()));
			onTagReadListener.onTagRead("");
		}
	}

	private String byte2HexString(byte[] bytes) {
		String ret = "";
		if (bytes != null) {
			for (Byte b : bytes) {
				ret += String.format("%02X", b.intValue() & 0xFF);
			}
		}
		return ret;
	}

	public String ndefRecordToString(NdefRecord record) {
		byte[] payload = record.getPayload();
		return new String(payload);
	}

	/**
	 * Writes a text to a tag
	 * @param context
	 * @param tag
	 * @param data
	 * @throws NFCWriteException
	 */
	protected void writeTag(Context context, Tag tag, String data) throws NFCWriteException {
		Log.i("Log : writeTag", data);
		// Record with actual data we care about
		NdefRecord relayRecord;
		if(data!="delete"){
			relayRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, null, data.getBytes());
		}
		else
			relayRecord = new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null);

		// Complete NDEF message with both records
		NdefMessage message = new NdefMessage(new NdefRecord[] { relayRecord });

		if(tag!=null)
		{
		Ndef ndef = Ndef.get(tag);

		if (ndef != null) {
			// If the tag is already formatted, just write the message to it
			try {
				System.out.println(ndef.isConnected()+tag.toString());
				if(!ndef.isConnected()) {
					ndef.connect();
				}
			} catch (IOException e) {
				System.out.println(e.toString());
				throw new NFCWriteException(NFCWriteException.NFCErrorType.unknownError);
			}
			// Make sure the tag is writable
			if (!ndef.isWritable()) {
				throw new NFCWriteException(NFCErrorType.ReadOnly);
			}

			// Check if there's enough space on the tag for the message
			int size = message.toByteArray().length;
			if (ndef.getMaxSize() < size) {
				throw new NFCWriteException(NFCErrorType.NoEnoughSpace);
			}

			try {
				// Write the data to the tag
				ndef.writeNdefMessage(message);
			} catch (TagLostException tle) {
				throw new NFCWriteException(NFCWriteException.NFCErrorType.tagLost, tle);
			} catch (IOException ioe) {
				throw new NFCWriteException(NFCErrorType.formattingError, ioe);// nfcFormattingErrorTitle
			} catch (FormatException fe) {
				throw new NFCWriteException(NFCErrorType.formattingError, fe);
			}

			try {

				ndef.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// If the tag is not formatted, format it with the message
			NdefFormatable format = NdefFormatable.get(tag);
			if (format != null) {
				System.out.println("Format"+format.toString());
				try {
					format.connect();
					format.format(message);
				} catch (TagLostException tle) {
					throw new NFCWriteException(NFCErrorType.tagLost, tle);
				} catch (IOException ioe) {
					throw new NFCWriteException(NFCErrorType.formattingError, ioe);
				} catch (FormatException fe) {
					throw new NFCWriteException(NFCErrorType.formattingError, fe);
				}
			} else {
				throw new NFCWriteException(NFCErrorType.noNdefError);
			}
		}

	}
	}

	public void formatNfc() throws NFCWriteException{
		Tag myTag = (Tag) activity.getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Ndef ndefTag = Ndef.get(myTag);
		NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null);


		// Complete NDEF message with both records
		NdefMessage message = new NdefMessage(new NdefRecord[] { relayRecord });
		System.out.println("Tech list"+myTag.getTechList().toString());
		// Complete NDEF message with both records

		NdefFormatable format = NdefFormatable.get(myTag);
		//System.out.println("Format"+format.equals(null));
		if (format != null) {
			try {
				//ndefTag.writeNdefMessage(new NdefMessage(new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)));
				format.connect();
				format.format(message);
			} catch (TagLostException tle) {
				throw new NFCWriteException(NFCErrorType.tagLost, tle);
			} catch (IOException ioe) {
				throw new NFCWriteException(NFCErrorType.formattingError, ioe);
			} catch (FormatException fe) {
				throw new NFCWriteException(NFCErrorType.formattingError, fe);
			}
		} else {
			throw new NFCWriteException(NFCErrorType.noNdefError);
		}
	}
	public interface TagReadListener {
		public void onTagRead(String tagRead);
	}

	public interface TagWriteListener {
		public void onTagWritten();
	}

	public interface TagWriteErrorListener {
		public void onTagWriteError(NFCWriteException exception);
	}


}
