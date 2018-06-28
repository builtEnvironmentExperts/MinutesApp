package com.example.pnpan.minutesapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;


public class CreateAgenda extends AppCompatActivity {
    EditText title;
    TextView date, time;
    EditText action_items, location;
    Button btn;

    int day, month, year;
    Calendar calendar;
    private File pdfFile;

    int selectedHour;
    int selectedMinute;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_agenda);
        btn = findViewById(R.id.butn_create_agenda);
        title = findViewById(R.id.title_input);
        date = findViewById(R.id.date_input);
        time = findViewById(R.id.time_input);
        location = findViewById(R.id.location_input);
        action_items = findViewById(R.id.action_items_input);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);

        showDate(year, month+1, day);

        time.setText("Time:" + " " + selectedHour + ":" + selectedMinute);

        time.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateAgenda.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        time.setText("Time:" + selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!valid()){

                    return;
                }
                try {
                    createPdfWrapper();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private boolean valid()
    {
        boolean bool = true;

        if(title.getText().toString().isEmpty())
        {
            title.setError("Title cannot be empty");
            bool = false;
        }
        if(date.getText().toString().isEmpty())
        {
            date.setError("Date cannot be empty");
            bool = false;
        }

        if(time.getText().toString().isEmpty())
        {
            time.setError("Time cannot be empty");
            bool = false;
        }
        if(action_items.getText().toString().isEmpty())
        {
            action_items.setError("Action items cannot be empty");
            bool = false;
        }
        return bool;
    }
    private void createPdfWrapper() throws FileNotFoundException,DocumentException {

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("You need to allow access to Storage",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }else {
            createPdf();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void createPdf() throws FileNotFoundException, DocumentException {

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            //Log.i(TAG, "Created a new directory for PDF");
        }

        pdfFile = new File(docsFolder.getAbsolutePath(),title.getText().toString()+".pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document();
        PdfWriter.getInstance(document, output);
        FontSelector selector1 = new FontSelector();
        Font f1 = FontFactory.getFont(FontFactory.TIMES_BOLD, 18);
        f1.setColor(BaseColor.BLACK);
        selector1.addFont(f1);

        FontSelector selector2 = new FontSelector();
        Font f2 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
        f2.setColor(BaseColor.BLACK);
        selector2.addFont(f2);


        com.itextpdf.text.Image img = null;

        try {
            Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.companylogo);
            Bitmap resized = Bitmap.createScaledBitmap(icon, (int)(icon.getWidth() * .50), (int)(icon.getHeight() * 0.50), true);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            resized.recycle();
            img = com.itextpdf.text.Image.getInstance(byteArray);
        }
        catch (Exception e)
        {

        }

        document.open();
        //document.add(icon); + \t
        img.setAlignment(Element.ALIGN_CENTER);
        document.add(img);
        String str1 = title.getText().toString();
        Phrase ph = selector1.process(str1);
        Paragraph p1 = new Paragraph(ph);
        document.add(p1);

        String date_str = "Date: " + date.getText().toString();
        Phrase date_phrase = selector2.process(date_str);
        Paragraph p2 = new Paragraph(date_phrase);
        document.add(new Paragraph(p2));

        String time_str  = time.getText().toString();
        Phrase time_phrase = selector2.process(time_str);
        Paragraph p3 = new Paragraph(time_phrase);
        document.add(p3);

        String location_str = "Location: " + location.getText().toString() + "\n";
        Phrase location_phrase = selector2.process(location_str);
        Paragraph p5 = new Paragraph(location_phrase);
        document.add(p5);

        String actiion_str = "Action items: \n " + action_items.getText().toString();
        Phrase action_phrase = selector2.process(actiion_str);
        Paragraph p4  = new Paragraph(action_phrase);
        document.add(p4);

        document.close();
        previewPdf();
    }

    private void previewPdf() {
        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(pdfFile);
            intent.setDataAndType(uri, "application/pdf");

            startActivity(intent);
        }else{
            Toast.makeText(this,"Download a PDF Viewer to see the generated PDF",Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
        Toast.makeText(getApplicationContext(), "ca",
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private void showDate(int year, int month, int day) {
        date.setText(new StringBuilder().append("Date: ").append(day).append("/")
                .append(month).append("/").append(year));
    }
}
