package com.zhaoyi.walker.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Created by jianjia Liu on 2017/4/6.
 */

public class InputDialogFragment extends DialogFragment {
    private InputListener listener;

    public static InputDialogFragment newInstance(int index) {
        InputDialogFragment inputDialogFragment = new InputDialogFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        inputDialogFragment.setArguments(args);
        return inputDialogFragment;
    }
    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(getActivity());

        switch (getArguments().getInt("index")) {
            case 0:
                final EditText editText = new EditText(getActivity());
                inputDialog.setView(editText);
                inputDialog.setPositiveButton(" 确定", new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener = (InputListener) getActivity();
                                listener.onInputComplete(editText.getText().toString().trim(), 0);
                            }
                        }).setNegativeButton("取消", null);
                break;
            case 1:
                final String[] items = { "A", "B", "AB", "O", "其他" };
                inputDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener = (InputListener) getActivity();
                        listener.onInputComplete(items[which].toString(), 1);
                    }
                });
                break;
            case 2:
                final String[] item = { "男", "女" };
                inputDialog.setItems(item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener = (InputListener) getActivity();
                        listener.onInputComplete(item[which].toString(), 2);
                    }
                });
                break;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                final EditText editTextOther = new EditText(getActivity());
                inputDialog.setView(editTextOther);
                inputDialog.setPositiveButton(" 确定", new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener = (InputListener) getActivity();
                                listener.onInputComplete(editTextOther.getText().toString().trim(),
                                        getArguments().getInt("index"));
                            }
                        }).setNegativeButton("取消", null);
                break;
            default:
                break;

        }
        return inputDialog.create();
    }
    public interface InputListener
    {
        void onInputComplete(Object obj, int index);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InputListener) {
            listener = (InputListener) context;
        } else {
            throw new RuntimeException(context.toString() +
                    "must implement InputListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
