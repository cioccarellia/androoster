package com.andreacioccarelli.androoster.ui.about;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.andreacioccarelli.androoster.R;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by andrea on 2017/nov.
 * Part of the package com.andreacioccarelli.androoster
 */

public class ChangelogDialog extends DialogFragment {

    public static ChangelogDialog create() {
        return new ChangelogDialog();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View customView;
        try {
            customView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_changelog, null);
        } catch (InflateException e) {
            e.printStackTrace();
            return new MaterialDialog.Builder(getActivity())
                    .title(android.R.string.dialog_alert_title)
                    .content(R.string.webview_unavailable)
                    .positiveText(R.string.action_ok)
                    .build();
        }
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.changelog_title)
                .customView(customView, false)
                .positiveText(android.R.string.cancel)
                .build();

        final WebView webView = customView.findViewById(R.id.web_view);
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = getActivity().getAssets().open("changelog.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null)
                buf.append(str);
            in.close();

            final String backgroundColor = colorToHex(ATHUtil.resolveColor(getActivity(), R.attr.md_background_color, Color.parseColor(ThemeSingleton.get().darkTheme ? "#424242" : "#ffffff")));
            final String contentColor = ThemeSingleton.get().darkTheme ? "#ffffff" : "#000000";
            webView.loadData(buf.toString()
                            .replace("{style-placeholder}",
                                    String.format("body { background-color: %s; color: %s; }", backgroundColor, contentColor))
                            .replace("{link-color}", colorToHex(ThemeSingleton.get().positiveColor.getDefaultColor()))
                            .replace("{link-color-active}", colorToHex(ColorUtil.lightenColor(ThemeSingleton.get().positiveColor.getDefaultColor())))
                    , "text/html", "UTF-8");
        } catch (Throwable e) {
            webView.loadData("<h1>Unable to load</h1><br><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
        }
        return dialog;
    }

    @NonNull
    private static String colorToHex(int color) {
        return Integer.toHexString(color).substring(2);
    }
}