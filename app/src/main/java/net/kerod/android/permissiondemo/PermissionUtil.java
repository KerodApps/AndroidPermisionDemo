package net.kerod.android.permissiondemo;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public abstract class PermissionUtil {
    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 17;
    public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 13;

    private static final String TAG = "PermissionUtil";


    public static void requestPermission(@NonNull AppCompatActivity activity, int requestCode, String permission, int rationale, boolean finishActivityIfPermissionDenied) {
          requestPermission ( activity,  requestCode,  permission, activity.getString(rationale), finishActivityIfPermissionDenied);
    }

    public static void requestPermission(@NonNull AppCompatActivity activity, int requestCode, @NonNull String requestedPermission, String rationale, boolean finishActivityIfPermissionDenied) {
        if (isPermissionGranted(requestedPermission)) {
            //we do not need to do anything! Permission asked previously and the user granted it.
        } else if (!SettingsManager.isPermissionRequested(requestedPermission)) {
            //user hasn't seen the permission request previously
            //Note: the caller AppCompatActivity has to implement onPermissionRequestResult to handle the result
            ActivityCompat.requestPermissions(activity, new String[]{requestedPermission}, requestCode);
            SettingsManager.setPermissionRequested(requestedPermission, true);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestedPermission)) {
            // 1)the app has requested this permission previously and the user has denied it.
            // 2)the user has first granted permission, and then revoked the permission by going to app settings
            //  but the user HASN'T ticked 'Don't show me again' since the latest grant'
            //
            //PermissionRationaleDialog dialog will manage requesting  the permission when 'OK' button clicked
            PermissionRationaleDialog.newInstance(requestCode, new String[]{requestedPermission}, rationale, finishActivityIfPermissionDenied).show(activity.getSupportFragmentManager(), "AAA");
        } else {//user ticks don't show me again check box previously!
            //If the user denied the permission and also ticked 'Don't show me again,' there is no way of
            //presenting a dialog to the user.
            // We can, however, assist the user to enable the permission by going to app settings
            PermissionEnableHelpDialog.newInstance(activity.getString(R.string.permission_enable_help_dialog),finishActivityIfPermissionDenied).show(activity.getSupportFragmentManager(), "");
        }
    }

    public static boolean isPermissionGranted(@NonNull String permission) {
        return (ContextCompat.checkSelfPermission(ApplicationManager.getAppContext(), permission) == PackageManager.PERMISSION_GRANTED);

    }

    /**
     * Checks if the result contains a {@link PackageManager#PERMISSION_GRANTED} result for a
     * permission from a runtime permissions request.
     *
     * @see ActivityCompat.OnRequestPermissionsResultCallback
     */
    public static boolean isPermissionGranted(@NonNull String[] grantPermissions, int[] grantResults, @NonNull String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }


    /**
     * A dialog that explains the use of the  permission and requests the necessary permission.
     * <p>
     * The activity should implement
     * {@link ActivityCompat.OnRequestPermissionsResultCallback}
     * to handle permit or denial of this permission request.
     */
    public static class PermissionRationaleDialog extends DialogFragment {

        private static final String ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode";
        private static final String ARGUMENT_PERMISSION_RATIONALE = "rationale";
        private static final String ARGUMENT_PERMISSION_NAME = "permissionName";
        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";
        //
        private boolean mFinishActivity = false;

        /**
         * Creates a new instance of a dialog displaying the rationale for the use of the  permission.
         * The permission is requested after clicking 'ok'.
         *
         * @param requestCode    Id of the request that is used to request the permission. It is
         *                       returned to the
         *                       {@link ActivityCompat.OnRequestPermissionsResultCallback}.
         * @param finishActivity Whether the calling Activity should be finished if the dialog is
         *                       cancelled.
         */
        @NonNull
        public static PermissionRationaleDialog newInstance(int requestCode, String[] permission, String rationale, boolean finishActivity) {
            Bundle arguments = new Bundle();
            arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode);
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);
            arguments.putStringArray(ARGUMENT_PERMISSION_NAME, permission);
            arguments.putString(ARGUMENT_PERMISSION_RATIONALE, rationale);

            PermissionRationaleDialog dialog = new PermissionRationaleDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            final int requestCode = arguments.getInt(ARGUMENT_PERMISSION_REQUEST_CODE);
            final String rationale = arguments.getString(ARGUMENT_PERMISSION_RATIONALE);
            final String[] permission = arguments.getStringArray(ARGUMENT_PERMISSION_NAME);
            mFinishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY);

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.permission_solicit_grant)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // After click on Ok, request the permission.
                            ActivityCompat.requestPermissions(getActivity(), permission, requestCode);
                            // Do not finish the Activity while requesting permission.
                            mFinishActivity = false;
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getActivity(),
                                    getString(R.string.permission_required_toast),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    })
                    .create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (mFinishActivity) {
                getActivity().finish();
            }
        }
    }

    /**
     * If the user denied the permission and also ticked 'Don't show me again,' there is no way of
     * presenting a dialog to the user. We can, however, assist the user to enable the permission by going to app settings
     *
     */
    public static class PermissionEnableHelpDialog extends DialogFragment {
        private static final String ARGUMENT_PERMISSION_RATIONALE = "rationale";
        private static final String ARGUMENT_FINISH_ON_PERMISSION_DENIED = "finishOnDeny";
        private static final String SCHEME_PACKAGE = "package";

        @NonNull
        public static PermissionEnableHelpDialog newInstance(String rationale, boolean finishAppOnPermissionDenied) {
            Bundle arguments = new Bundle();
            arguments.putString(ARGUMENT_PERMISSION_RATIONALE, rationale);
            arguments.putBoolean(ARGUMENT_FINISH_ON_PERMISSION_DENIED, finishAppOnPermissionDenied);

            PermissionEnableHelpDialog dialog = new PermissionEnableHelpDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            final String rationale = arguments.getString(ARGUMENT_PERMISSION_RATIONALE);
            final boolean finishOnDeny = arguments.getBoolean(ARGUMENT_FINISH_ON_PERMISSION_DENIED);
            final Activity activity = getActivity();

            return new AlertDialog.Builder(activity)
                    .setTitle(R.string.permission_solicit_grant)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//didnt work
                            Uri uri = Uri.fromParts(SCHEME_PACKAGE, activity.getPackageName(), null);
                            intent.setData(uri);
                            activity.startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(finishOnDeny){
                                 Toast.makeText(getActivity(), getString(R.string.permission_required_toast), Toast.LENGTH_LONG).show();
                                 getActivity().finish();
                            }else{
                                Toast.makeText(getActivity(), getString(R.string.permission_required_toast), Toast.LENGTH_LONG).show();

                            }
                        }
                    })
                    .create();
        }


    }
}
