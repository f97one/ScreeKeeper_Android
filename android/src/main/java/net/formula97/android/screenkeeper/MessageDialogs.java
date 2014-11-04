package net.formula97.android.screenkeeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.EventListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link net.formula97.android.screenkeeper.MessageDialogs.OnButtonSelectionListener} interface
 * to handle interaction events.
 * Use the {@link MessageDialogs#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageDialogs extends DialogFragment {

    public static final String FRAGMENT_KEY = MessageDialogs.class.getName();

    public static final int PRESSED_NEGATIVE = 0;
    public static final int PRESSED_POSITIVE =1;

    public static final int SET_POSITIVE_BUTTON = 11;
    public static final int SET_NEGATIVE_BUTTON = 12;
    public static final int SET_BOTH_BUTTON = 13;

    private static final String MESSAGE_BODY_KEY = "MessageBodyKey";
    private static final String MESSAGE_TITLE_KEY = "MessageTitleKey";
    private static final String BUTTON_SET_KEY = "ButtonSetKey";

    private OnButtonSelectionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param messageBody Parameter 1.
     * @param messageTitle Parameter 2.
     * @return A new instance of fragment MessageDialogs.
     */
    public static MessageDialogs newInstance(String messageBody, String messageTitle, int buttonSet) {
        MessageDialogs fragment = new MessageDialogs();
        Bundle args = new Bundle();
        args.putString(MESSAGE_BODY_KEY, messageBody);
        args.putString(MESSAGE_TITLE_KEY, messageTitle);
        args.putInt(BUTTON_SET_KEY, buttonSet);
        fragment.setArguments(args);
        return fragment;
    }

    public MessageDialogs() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnButtonSelectionListener) {
            mListener = (OnButtonSelectionListener) activity;
        } else {
            mListener = null;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String body = getArguments().getString(MESSAGE_BODY_KEY);
        String title = getArguments().getString(MESSAGE_TITLE_KEY);
        int buttonSet = getArguments().getInt(BUTTON_SET_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(body);

        switch (buttonSet) {
            case SET_POSITIVE_BUTTON:
                makePositiveButton(builder, body);
                break;
            case SET_NEGATIVE_BUTTON:
                makeNegativeButton(builder, body);
                break;
            case SET_BOTH_BUTTON:
                makePositiveButton(builder, body);
                makeNegativeButton(builder, body);
                break;
        }

        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void makePositiveButton(final AlertDialog.Builder builder, final String messageBody) {
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onButtonSelected(messageBody, PRESSED_POSITIVE);
                }
                getDialog().dismiss();
            }
        });
    }

    private void makeNegativeButton(final AlertDialog.Builder builder, final String messageBody) {
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onButtonSelected(messageBody, PRESSED_NEGATIVE);
                }
                getDialog().dismiss();
            }
        });
    }

    /**
     *
     */
    public interface OnButtonSelectionListener extends EventListener {
        public void onButtonSelected(String messageBody, int whichButton);
    }

}
