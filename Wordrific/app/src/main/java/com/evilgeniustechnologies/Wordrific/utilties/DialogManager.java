package com.evilgeniustechnologies.Wordrific.utilties;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.evilgeniustechnologies.Wordrific.R;

/**
 * Created by vendetta on 5/30/14.
 */
public class DialogManager {
    private static Alert alert = Alert.NULL;
    private static DawnDialog dialog;
    private static ProgressDialog progress;
    private static String message;
    private static String title;

    public static void setAlert(Alert alert) {
        if (DialogManager.alert != alert) {
            closeProgress();
            DialogManager.alert = alert;
        }
    }

    public static void setMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            closeProgress();
            DialogManager.message = message;
            alert = Alert.MESSAGE;
        }
    }

    public static void show(Context context, Alert alert) {
        if (DialogManager.alert != alert) {
            closeProgress();
            DialogManager.alert = alert;
        }
        show(context);
    }

    public static void show(Context context, Alert alert, String message) {
        if (DialogManager.alert != alert) {
            closeProgress();
            DialogManager.message = message;
            DialogManager.alert = alert;
        }
        show(context);
    }

    public static void show(Context context, String message) {
        if (!TextUtils.isEmpty(message)) {
            closeProgress();
            DialogManager.message = message;
            DialogManager.title = context.getString(R.string.app_name);
            alert = Alert.MESSAGE;
            show(context);
        }
    }

    public static void show(Context context, Alert alert, String message, String title) {
        if (DialogManager.alert != alert && !TextUtils.isEmpty(message)) {
            closeProgress();
            DialogManager.message = message;
            DialogManager.title = title;
            DialogManager.alert = alert;
            show(context);
        }
    }

    public static boolean show(final Context context) {
        Bundle args = new Bundle();
        switch (alert) {
            case NULL: message = null;
                break;
            case NO_INTERNET:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.no_internet));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, R.string.no_internet);
                message = null;
                return true;
            case PROGRESS:
                showProgress(context);
                message = null;
                return true;
            case PROGRESS_BAR:
                showProgressBar(context);
                message = null;
                return true;
            case TIME_OUT:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.game_over));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog() {
                    @Override
                    public void onClicked() {
                        ((Activity) context).finish();
                    }
                };
                dialog.setArguments(args);
                showDialog(context, R.string.app_name);
                message = null;
                return true;
            case NO_EMAIL:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.no_email));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, R.string.no_email);
                message = null;
                return true;
            case NO_PASSWORD:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.no_password));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, R.string.no_password);
                message = null;
                return true;
            case MESSAGE:
                args.putString(DawnDialog.MESSAGE, message);
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, message);
                message = null;
                return true;
            case LOGIN_SUCCESS:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.login_success));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog() {
                    @Override
                    public void onClicked() {
                        ((Activity) context).setResult(Activity.RESULT_OK);
                        ((Activity) context).finish();
                    }
                };
                dialog.setArguments(args);
                showDialog(context, R.string.login_success);
                message = null;
                return true;
            case NO_FIRST_NAME:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.no_first_name));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, R.string.no_first_name);
                message = null;
                return true;
            case NO_LAST_NAME:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.no_last_name));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, R.string.no_last_name);
                message = null;
                return true;
            case NO_RETRY_PASSWORD:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.no_password));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, R.string.no_retry_password);
                message = null;
                return true;
            case INVALID_EMAIL_ADDRESS:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.invalid_email_address));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, R.string.invalid_email_address);
                message = null;
                return true;
            case PASSWORD_MISMATCH:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.password_mismatch));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, R.string.password_mismatch);
                message = null;
                return true;
            case REGISTER_SUCCESS:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.register_success));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog() {
                    @Override
                    public void onClicked() {
                        ((Activity) context).setResult(Activity.RESULT_OK);
                        ((Activity) context).finish();
                    }
                };
                dialog.setArguments(args);
                showDialog(context, R.string.register_success);
                message = null;
                return true;
            case MAKE_FRIEND:
                args.putString(DawnDialog.MESSAGE, context.getString(R.string.make_friend));
                args.putString(DawnDialog.TITLE, context.getString(R.string.app_name));
                dialog = new DawnDialog();
                dialog.setArguments(args);
                showDialog(context, R.string.make_friend);
                message = null;
                return true;
            case GAME_FINISH:
                args.putString(DawnDialog.MESSAGE, message);
                args.putString(DawnDialog.TITLE, title);
                dialog = new DawnDialog() {
                    @Override
                    public void onClicked() {
                        ((Activity) context).finish();
                    }
                };
                dialog.setArguments(args);
                showDialog(context, R.string.app_name);
                message = null;
                return true;
        }
        message = null;
        return false;
    }

    private static void showDialog(Context context, int tagId) {
        dialog.show(((Activity) context).getFragmentManager(), context.getString(tagId));
        reset();
    }

    private static void showDialog(Context context, String message) {
        dialog.show(((Activity) context).getFragmentManager(), message);
        reset();
    }

    private static void showProgress(Context context) {
        if (progress == null) {
            progress = new ProgressDialog(context);
            progress.setMessage(context.getString(R.string.progress));
            progress.setIndeterminate(true);
            progress.setCancelable(false);
        }
        if (!progress.isShowing()) {
            progress.show();
        }
    }

    private static void showProgressBar(Context context) {
        if (progress == null) {
            progress = new ProgressDialog(context);
            if (TextUtils.isEmpty(message)) {
                progress.setMessage(context.getString(R.string.check_for_update));
            } else {
                progress.setMessage(message);
            }
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setProgress(0);
            progress.setMax(100);
            progress.setCancelable(false);
        }
        if (!progress.isShowing()) {
            progress.show();
        }
    }

    public static void close() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    public static void closeProgress() {
        if (alert == Alert.PROGRESS || alert == Alert.PROGRESS_BAR) {
            alert = Alert.NULL;
        }
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    public static void updateProgress(int value) {
        if (progress != null && progress.isShowing()) {
            progress.setProgress(value);
            if (value >= 100) {
                closeProgress();
            }
        }
    }

    public static void reset() {
        alert = Alert.NULL;
    }

    public enum Alert {
        NULL,
        NO_INTERNET,
        PROGRESS,
        PROGRESS_BAR,
        TIME_OUT,
        MESSAGE,
        LOGIN_SUCCESS,
        NO_EMAIL,
        NO_PASSWORD,
        NO_FIRST_NAME,
        NO_LAST_NAME,
        NO_RETRY_PASSWORD,
        INVALID_EMAIL_ADDRESS,
        PASSWORD_MISMATCH,
        REGISTER_SUCCESS,
        MAKE_FRIEND,
        GAME_FINISH,
    }
}
