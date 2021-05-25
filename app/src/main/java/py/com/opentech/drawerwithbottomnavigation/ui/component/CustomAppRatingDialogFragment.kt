package py.com.opentech.drawerwithbottomnavigation.ui.component

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hosseiniseyro.apprating.*
import com.hosseiniseyro.apprating.extensions.applyIfNotZero


class CustomAppRatingDialogFragment : DialogFragment() {

    private var listener: CustomRatingDialogListener? = null
        get() {
            if (host is CustomRatingDialogListener) {
                return host as CustomRatingDialogListener
            }
            return targetFragment as CustomRatingDialogListener?
        }

    private lateinit var data: CustomAppRatingDialog.Builder.Data
    private lateinit var alertDialog: AlertDialog
    private lateinit var dialogView: AppRatingDialogView

    private val title by lazy { data.title.resolveText(resources) }
    private val description by lazy { data.description.resolveText(resources) }
    private val defaultComment by lazy { data.defaultComment.resolveText(resources) }
    private val hint by lazy { data.hint.resolveText(resources) }
    private val positiveButtonText by lazy { data.positiveButtonText.resolveText(resources) }
    private val neutralButtonText by lazy { data.neutralButtonText.resolveText(resources) }
    private val negativeButtonText by lazy { data.negativeButtonText.resolveText(resources) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return setupAlertDialog(requireActivity())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat(CURRENT_RATE_NUMBER, dialogView.rateNumber)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val rateNumber: Float? = savedInstanceState?.getFloat(CURRENT_RATE_NUMBER)
        if (rateNumber != null) {
            dialogView.setDefaultRating(rateNumber.toInt())
        }
    }

    private fun setupAlertDialog(context: Context): AlertDialog {
        dialogView = AppRatingDialogView(context)
        val builder = AlertDialog.Builder(requireActivity())
        data = arguments?.getSerializable(DIALOG_DATA) as CustomAppRatingDialog.Builder.Data


        setupPositiveButton(dialogView, builder)
        setupNegativeButton(builder)
        setupNeutralButton(builder)
        setupTitleAndContentMessages(dialogView)
        setupHint(dialogView)
        setupColors(dialogView)
        setupInputBox()
        setupRatingBar()

        builder.setView(dialogView)
        alertDialog = builder.create()
        if (data.dialogBackgroundColorResId != 0) {
            alertDialog.window?.setBackgroundDrawableResource(data.dialogBackgroundColorResId)
        }

        setupAnimation()
        setupCancelable()
        return alertDialog
    }

    override fun onResume() {
        super.onResume()
        val d = dialog as AlertDialog?
        if (d != null) {
            var wantToCloseDialog = false

            val positiveButton: Button = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            positiveButton.setOnClickListener(View.OnClickListener {
                if (!TextUtils.isEmpty(positiveButtonText)) {
                    val rateNumber = dialogView.rateNumber.toInt()
                    if (rateNumber == 0) {
                        listener?.onNoneChoose()
                    } else {
                        if (rateNumber <= data.threshold) {
                            val comment = dialogView.comment
                            listener?.onPositiveButtonClickedWithComment(rateNumber, comment)
                        } else { //rating>threshold
                            listener?.onPositiveButtonClickedWithoutComment(rateNumber)
                        }

                        d.dismiss()
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.

                    }
                }
            })

        }
    }

    private fun setupRatingBar() {
        dialogView.setNumberOfStars(data.numberOfStars)

        val isEmpty = data.noteDescriptions?.isEmpty() ?: true
        if (!isEmpty) {
            dialogView.setNoteDescriptions(data.noteDescriptions!!)
        }

        dialogView.setDefaultRating(data.defaultRating)
    }

    private fun setupInputBox() {
        dialogView.setCommentInputEnabled(data.commentInputEnabled)
        dialogView.setThreshold(data.threshold)
    }

    private fun setupCancelable() {
        data.cancelable?.let { isCancelable = it }
        data.canceledOnTouchOutside?.let { alertDialog.setCanceledOnTouchOutside(it) }
    }

    private fun setupAnimation() {
        if (data.windowAnimationResId != 0) {
            alertDialog.window?.attributes?.windowAnimations = data.windowAnimationResId
        }
    }

    private fun setupColors(dialogView: AppRatingDialogView) {
        data.titleTextColorResId.applyIfNotZero {
            dialogView.setTitleTextColor(this)
        }
        data.descriptionTextColorResId.applyIfNotZero {
            dialogView.setDescriptionTextColor(this)
        }
        data.commentTextColorResId.applyIfNotZero {
            dialogView.setEditTextColor(this)
        }
        data.commentBackgroundColorResId.applyIfNotZero {
            dialogView.setEditBackgroundColor(this)
        }
        data.hintTextColorResId.applyIfNotZero {
            dialogView.setHintColor(this)
        }
        data.starColorResId.applyIfNotZero {
            dialogView.setStarColor(this)
        }
        data.noteDescriptionTextColor.applyIfNotZero {
            dialogView.setNoteDescriptionTextColor(this)
        }
    }

    private fun setupTitleAndContentMessages(dialogView: AppRatingDialogView) {
        if (!title.isNullOrEmpty()) {
            dialogView.setTitleText(title!!)
        }
        if (!description.isNullOrEmpty()) {
            dialogView.setDescriptionText(description!!)
        }
        if (!defaultComment.isNullOrEmpty()) {
            dialogView.setDefaultComment(defaultComment!!)
        }
    }

    private fun setupHint(dialogView: AppRatingDialogView) {
        if (!TextUtils.isEmpty(hint)) {
            dialogView.setHint(hint!!)
        }
    }

    private fun setupNegativeButton(builder: AlertDialog.Builder) {
        if (!TextUtils.isEmpty(negativeButtonText)) {
            builder.setNegativeButton(negativeButtonText) { _, _ ->
//                PreferenceHelper.setAgreeShowDialog(context, false)
                listener?.onNegativeButtonClicked()
            }
        }
    }

    private fun setupPositiveButton(dialogView: AppRatingDialogView, builder: AlertDialog.Builder) {
        if (!TextUtils.isEmpty(positiveButtonText)) {
            builder.setPositiveButton(positiveButtonText) { _, _ ->
                val rateNumber = dialogView.rateNumber.toInt()
                if (rateNumber == 0) {
                    listener?.onNoneChoose()
                } else {
                    if (rateNumber <= data.threshold) {
                        val comment = dialogView.comment
                        listener?.onPositiveButtonClickedWithComment(rateNumber, comment)
                    } else { //rating>threshold
                        listener?.onPositiveButtonClickedWithoutComment(rateNumber)
                    }
                }

            }
        }
    }

    private fun setupNeutralButton(builder: AlertDialog.Builder) {
        if (!TextUtils.isEmpty(neutralButtonText)) {
            builder.setNeutralButton(neutralButtonText) { _, _ ->
//                PreferenceHelper.setRemindInterval(context)
                listener?.onNeutralButtonClicked()
            }
        }
    }

    companion object {

        fun newInstance(data: CustomAppRatingDialog.Builder.Data): CustomAppRatingDialogFragment {
            val fragment = CustomAppRatingDialogFragment()
            val bundle = Bundle()
            bundle.putSerializable(DIALOG_DATA, data)
            fragment.arguments = bundle
            return fragment
        }
    }
}
