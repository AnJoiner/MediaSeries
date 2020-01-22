package com.coder.media.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.coder.media.R
import java.lang.ref.WeakReference

/**
 *
 * @author: AnJoiner
 * @datetime: 20-1-22
 */

class CustomProgressDialog(context: Context, themeResId: Int) : Dialog(context, themeResId),
    DialogInterface.OnCancelListener {


    private var mContext =
        WeakReference<Context?>(null)
    @Volatile
    private var sDialog: CustomProgressDialog? = null


    private fun create(
        context: Context,
        message: CharSequence?
    ): CustomProgressDialog? {
        CustomProgressDialog(context, R.style.CustomProgressDialog)
        mContext = WeakReference(context)
        @SuppressLint("InflateParams") val view: View =
            LayoutInflater.from(context).inflate(R.layout.dialog_custom_progress, null)
        val tvMessage = view.findViewById<View>(R.id.tv_message) as TextView
        if (!TextUtils.isEmpty(message)) {
            tvMessage.text = message
        }
        val lp = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        addContentView(view, lp)
        setOnCancelListener(this)
        return this
    }

    override fun onCancel(dialog: DialogInterface?) { // 点手机返回键等触发Dialog消失，应该取消正在进行的网络请求等
        val context = mContext.get()
        if (context != null) { //            Toast.makeText(context, "cancel", Toast.LENGTH_SHORT).show();
        }
    }

    @Synchronized
    fun showLoading(context: Context?) {
        showLoading(context, "loading...")
    }

    @Synchronized
    fun showLoading(
        context: Context?,
        cancelable: Boolean
    ) {
        showLoading(context, "loading...", cancelable)
    }

    @Synchronized
    fun showLoading(
        context: Context?,
        message: CharSequence?
    ) {
        showLoading(context, message, true)
    }

    @Synchronized
    fun showLoading(
        context: Context?,
        message: CharSequence?,
        cancelable: Boolean
    ) {
        if (sDialog != null && sDialog!!.isShowing) {
            sDialog!!.dismiss()
        }
        if (context == null || context !is Activity) {
            return
        }
        sDialog = create(context, message)
        sDialog!!.setCancelable(cancelable)
        if (sDialog != null && !sDialog!!.isShowing && !context.isFinishing) {
            sDialog!!.show()
        }
    }

    @Synchronized
    fun stopLoading() {
        if (sDialog != null && sDialog!!.isShowing) {
            sDialog!!.dismiss()
        }
        sDialog = null
    }
}