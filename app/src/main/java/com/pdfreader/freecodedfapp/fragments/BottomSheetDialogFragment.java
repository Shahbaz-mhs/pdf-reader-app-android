package com.pdfreader.freecodedfapp.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.pdfreader.freecodedfapp.BuildConfig;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.data.DbHelper;
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent;
import com.pdfreader.freecodedfapp.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

public class BottomSheetDialogFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment implements View.OnClickListener {
    public static final String FROM_RECENT = "com.example.slimpdfapp.FROM_RECENT";
    public static final String PDF_PATH = "com.example.slimpdfapp.PDF_PATH";
    public final String TAG = BottomSheetDialogFragment.class.getSimpleName();
    Context context;
    String fileName;
    Boolean fromRecent;
    String pdfPath;
    AppCompatImageView toggleStared;
    View.OnClickListener toggleStaredListener = new View.OnClickListener() {
        public void onClick(View view) {
            BottomSheetDialogFragment.this.dismiss();
            DbHelper instance = DbHelper.getInstance(BottomSheetDialogFragment.this.context);
            if (instance.isStared(BottomSheetDialogFragment.this.pdfPath)) {
                instance.removeStaredPDF(BottomSheetDialogFragment.this.pdfPath);
            } else {
                instance.addStaredPDF(BottomSheetDialogFragment.this.pdfPath);
            }
            EventBus.getDefault().post(new DataUpdatedEvent.RecentPDFStaredEvent());
            EventBus.getDefault().post(new DataUpdatedEvent.DevicePDFStaredEvent());


        }
    };
    onClickLisner onClickLisner;

    @SuppressLint("RestrictedApi")
    public void setupDialog(Dialog dialog, int i) {
        super.setupDialog(dialog, i);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.pdfPath = arguments.getString("com.example.slimpdfapp.PDF_PATH");
            this.fileName = new File(this.pdfPath).getName();
            this.fromRecent = arguments.getBoolean(FROM_RECENT);
        }
        this.context = getContext();
        if (this.context != null) {
            View inflate = View.inflate(this.context, R.layout.fragment_bottom_sheet_dialog, null);
            dialog.setContentView(inflate);
            this.toggleStared = inflate.findViewById(R.id.toggle_star);
            ((TextView) inflate.findViewById(R.id.file_name)).setText(this.fileName);
            setupStared();
            inflate.findViewById(R.id.action_share_container).setOnClickListener(this);
            inflate.findViewById(R.id.action_edit_container).setOnClickListener(this);
            inflate.findViewById(R.id.action_pdf_tools_container).setOnClickListener(this);
            inflate.findViewById(R.id.action_print_container).setOnClickListener(this);
            inflate.findViewById(R.id.action_security_container).setOnClickListener(this);
            inflate.findViewById(R.id.action_delete_container).setOnClickListener(this);
            inflate.findViewById(R.id.action_share_as_picture_container).setOnClickListener(this);
            inflate.findViewById(R.id.action_location_container).setOnClickListener(this);
            this.toggleStared.setOnClickListener(this.toggleStaredListener);
        }
    }

    @Override
    public int getTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return R.style.BottomSheetDialogTheme;
        }
        return super.getTheme();
    }



    @Override
    public void onClick(View view) {
        int id2 = view.getId();
        Toast.makeText(this.context, "Action ID: " + id2, Toast.LENGTH_SHORT).show();
        if (view != null) {
            int id = view.getId();
            switch (id) {
                case 2131361864:  //R.id.action_delete_container
                    Toast.makeText(this.context, "Delete", Toast.LENGTH_SHORT).show();
                    deletePdfFile();
                    break;
                case 2131361867:  //R.id.action_edit_container
                    Toast.makeText(this.context, "rename", Toast.LENGTH_SHORT).show();
                    renamePdf();
                    break;
                case 2131361871: //R.id.action_location_container:
                    if (pdfPath != null) {
                        Toast.makeText(this.context, this.pdfPath, Toast.LENGTH_LONG).show();
                    }
                    break;
//                case R.id.action_pdf_tools_container:
//                    showPdfTools();
//                    break;
                case 2131361884: //R.id.action_print_container:
                    if (pdfPath != null && context != null) {
                        Uri printUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(this.pdfPath));
                        Utils.print(this.context, printUri);
                    }
                    break;
//                case R.id.action_security_container:
//                    showPdfSecurityDetails(this.pdfPath);
//                    break;
//                case R.id.action_share_as_picture_container:
//                    if (pdfPath != null && context != null) {
//                        Uri pictureUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(this.pdfPath));
//                        showShareAsPicture(pictureUri);
//                    }
//                    break;
                case 2131361897: //R.id.action_share_container:
                    if (pdfPath != null && context != null) {
                        Uri shareUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(pdfPath));
                        ArrayList<Uri> uris = new ArrayList<>();
                        uris.add(shareUri);
                        //Toast.makeText(this.context, "Sharing: " + pdfPath, Toast.LENGTH_LONG).show();
                        shareFile(uris);
                    }
                    break;
                default:
                    break;
            }
            // Optionally call dismiss() here if you want to close the dialog after an action
            // dismiss();
        }
    }



    private void shareFile(ArrayList<Uri> uris) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("application/pdf");
        startActivity(Intent.createChooser(intent, "Select app to send message…"));
    }

    public void setupStared() {
        if (DbHelper.getInstance(this.context).isStared(this.pdfPath)) {
            this.toggleStared.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_bookmark2));
        }
    }

    public BottomSheetDialogFragment(BottomSheetDialogFragment.onClickLisner onClickLisner) {
        this.onClickLisner = onClickLisner;
    }

    public void deletePdfFile() {
        showConfirmDialog();
    }

    public void showConfirmDialog() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this.context);
        materialAlertDialogBuilder.setTitle((int) R.string.permanently_delete_file).setPositiveButton((int) R.string.delete, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                onClickLisner.onClick();
                BottomSheetDialogFragment.this.lambda$showConfirmDialog$1$BottomSheetDialogFragment(dialogInterface, i);
            }
        }).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) null);
        materialAlertDialogBuilder.show();
    }

    public void renamePdf() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this.context);
        File file = new File(this.pdfPath);
        String removeExtension = Utils.removeExtension(file.getName());
        materialAlertDialogBuilder.setView((int) R.layout.dialog_edit_text);
        materialAlertDialogBuilder.setTitle((int) R.string.rename_file).setPositiveButton((int) R.string.ok, (DialogInterface.OnClickListener) null).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) null);
        AlertDialog create = materialAlertDialogBuilder.create();
        create.show();
        TextInputEditText textInputEditText = (TextInputEditText) create.findViewById(R.id.input_text);
        if (textInputEditText != null) {
            textInputEditText.setText(removeExtension);
            textInputEditText.setSelectAllOnFocus(true);
        }
        create.getButton(-1).setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ TextInputEditText f$1;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ File f$3;
            public final /* synthetic */ AlertDialog f$4;

            {
                this.f$1 = textInputEditText;
                this.f$2 = removeExtension;
                this.f$3 = file;
                this.f$4 = create;
            }

            public final void onClick(View view) {


                BottomSheetDialogFragment.this.lambda$renamePdf$3$BottomSheetDialogFragment(this.f$1, this.f$2, this.f$3, this.f$4, view);
            }
        });
    }

    public /* synthetic */ void lambda$showConfirmDialog$1$BottomSheetDialogFragment(DialogInterface dialogInterface, int i) {
        Log.d(this.TAG, "Delete from device");
        File file = new File(this.pdfPath);
        if (file.delete()) {
            new File(this.context.getCacheDir() + "/Thumbnails/" + Utils.removeExtension(file.getName()) + ".jpg").delete();
            MediaScannerConnection.scanFile(this.context, new String[]{this.pdfPath}, (String[]) null, new MediaScannerConnection.OnScanCompletedListener() {
                public final void onScanCompleted(String str, Uri uri) {
                    BottomSheetDialogFragment.this.lambda$null$0$BottomSheetDialogFragment(str, uri);
                }
            });
            return;
        }
        Toast.makeText(this.context, "Can't delete file", Toast.LENGTH_LONG).show();
    }

    public /* synthetic */ void lambda$null$0$BottomSheetDialogFragment(String str, Uri uri) {
        EventBus.getDefault().post(new DataUpdatedEvent.PermanetlyDeleteEvent());
        String str2 = this.TAG;
        Log.d(str2, "File deleted " + this.pdfPath);
    }

    public /* synthetic */ void lambda$renamePdf$3$BottomSheetDialogFragment(TextInputEditText textInputEditText, String str, File file, AlertDialog alertDialog, View view) {
        String obj = textInputEditText.getText().toString();
        if (TextUtils.equals(str, obj)) {
            alertDialog.dismiss();
            Log.d(this.TAG, "File name not changed so do nothing");
        } else if (Utils.isFileNameValid(obj)) {
            String replace = this.pdfPath.replace(str, obj);
            if (file.renameTo(new File(replace))) {
                alertDialog.dismiss();
                DbHelper instance = DbHelper.getInstance(this.context);
                instance.updateHistory(this.pdfPath, replace);
                instance.updateStaredPDF(this.pdfPath, replace);
                instance.updateBookmarkPath(this.pdfPath, replace);
                instance.updateLastOpenedPagePath(this.pdfPath, replace);
                String str2 = this.context.getCacheDir() + "/Thumbnails/";
                String str3 = str2 + Utils.removeExtension(file.getName()) + ".jpg";
                String str4 = str2 + Utils.removeExtension(obj) + ".jpg";
                Log.d(this.TAG, "Rename thumbnail from " + str3);
                Log.d(this.TAG, "Rename thumbnail to " + str4);
                new File(str3).renameTo(new File(str4));
                MediaScannerConnection.scanFile(this.context, new String[]{replace}, (String[]) null, new MediaScannerConnection.OnScanCompletedListener() {
                    public final /* synthetic */ String f$1;

                    {
                        this.f$1 = replace;
                    }

                    public final void onScanCompleted(String str, Uri uri) {
                        onClickLisner.onClickRename(textInputEditText.getText().toString(), f$1);
                        BottomSheetDialogFragment.this.lambda$null$2$BottomSheetDialogFragment(this.f$1, str, uri);
                    }
                });
                return;
            }
            Toast.makeText(this.context, R.string.failed_to_rename_file, Toast.LENGTH_LONG).show();
        } else {
            textInputEditText.setError(this.context.getString(R.string.invalid_file_name));
        }
    }

    public void lambda$null$2$BottomSheetDialogFragment(String str, String str2, Uri uri) {
        EventBus.getDefault().post(new DataUpdatedEvent.PdfRenameEvent());
        String str3 = this.TAG;
        Log.d(str3, "Old pdf path" + this.pdfPath);
        String str4 = this.TAG;
        Log.d(str4, "New pdf path" + str);
    }

    public interface onClickLisner {
        void onClick();

        void onClickRename(String s, String name);

    }

    public void showShareAsPicture(Uri uri) {

    }

    public void showPdfTools() {

    }

    public void showPdfSecurityDetails(String str) {

    }
}
