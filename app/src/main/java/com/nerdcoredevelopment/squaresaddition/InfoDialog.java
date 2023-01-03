package com.nerdcoredevelopment.squaresaddition;

import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

public class InfoDialog extends Dialog {
    private AppCompatTextView infoText;
    private LinearLayout infoButtonsLinearLayout;
    private AppCompatButton infoContinueButton;
    private void initialise() {
        infoText = findViewById(R.id.info_text);
        infoButtonsLinearLayout = findViewById(R.id.info_buttons_linear_layout);
        infoContinueButton = findViewById(R.id.info_continue);
    }

    private void setVisibilityOfViews(int visibility) {
        infoText.setVisibility(visibility);
        infoButtonsLinearLayout.setVisibility(visibility);
    }

    public InfoDialog(@NonNull Context context, String infoText) {
        super(context, R.style.CustomDialogTheme);
        setContentView(R.layout.dialog_info);

        initialise();

        this.infoText.setText(infoText);

        infoContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // First, the views will disappear, then the dialog box will close
                setVisibilityOfViews(View.INVISIBLE);
                new CountDownTimer(100, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {}
                    @Override
                    public void onFinish() {
                        dismiss();
                    }
                }.start();
            }
        });
    }

    @Override
    public void show() {
        // Set the dialog to not focusable.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        // Show the dialog!
        super.show();

        // Set the dialog to immersive sticky mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Clear the not focusable flag from the window
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        // First, the dialog box will open, then the views will show
        new CountDownTimer(400, 400) {
            @Override
            public void onTick(long millisUntilFinished) {}
            @Override
            public void onFinish() {
                setVisibilityOfViews(View.VISIBLE);
            }
        }.start();
    }
}
