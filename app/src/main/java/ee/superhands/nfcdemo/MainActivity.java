package ee.superhands.nfcdemo;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private Toast lastToast;

    private void tagScanComplete(String tagId, String payload) {
        if(lastToast != null){
            lastToast.cancel();
        }

        Toast toast = Toast.makeText(this, null, Toast.LENGTH_LONG);
        toast.setText(tagId + System.getProperty("line.separator") + payload);
        toast.show();

        lastToast = toast;
    }

    private static String bytesToHexString(byte[] bytes, char separator) {
        String s = "0";
        StringBuilder hexString = new StringBuilder();
        if ((bytes != null) && (bytes.length > 0)) {
            for (byte b : bytes) {
                int n = b & 0xff;
                if (n < 0x10) {
                    hexString.append("0");
                }
                hexString.append(Integer.toHexString(n));
                if (separator != 0) {
                    hexString.append(separator);
                }
            }
            s = hexString.substring(0, hexString.length() - 1);
        }
        return s;
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tag != null){
                String tagId = bytesToHexString(tag.getId(), ':');

                try {
                    Parcelable[] ndefMessageArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                    for(Parcelable parcelable : ndefMessageArray) {
                        NdefMessage ndefMessage = (NdefMessage)parcelable;

                        for (NdefRecord record : ndefMessage.getRecords()) {
                            if (record.getTnf() == 1) {
                                byte[] rawPayload = record.getPayload();
                                String payload = new String(rawPayload, 3, rawPayload.length - 3).trim(); //Get the payload text!
                                tagScanComplete(tagId, payload);
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[][] techList = new String[][]{new String[]{"android.nfc.tech.NfcA", "android.nfc.tech.MifareUltralight", "android.nfc.tech.Ndef"}};
        IntentFilter[] filters = new IntentFilter[] {new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techList);
    }
}
