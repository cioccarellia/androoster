package com.andreacioccarelli.androoster.tools

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.jrummyapps.android.shell.Shell

/**
 * Created by andrea on 2018/May.
 * Part of the package com.andreacioccarelli.androoster.tools
 */
object RebootDialog {
    fun show(context: Context) {
        MaterialDialog.Builder(context)
                .title(R.string.dialog_reboot_title)
                .positiveText(R.string.action_confirm)
                .items(R.array.reboot)
                .itemsCallbackSingleChoice(-1) { dialog, view, which, text ->

                    when (which) {
                        0 -> Shell.SU.run("reboot")
                        1 -> Shell.SU.run("reboot recovery")
                        2 -> Shell.SU.run("reboot bootloader")
                        3 -> Shell.SU.run("setprop ctl.restart zygote","killall system_server")
                    }

                    true
                }
                .show()
    }
}
