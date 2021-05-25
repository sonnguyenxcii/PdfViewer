package py.com.opentech.drawerwithbottomnavigation.ui.component

interface CustomRatingDialogListener {

    fun onPositiveButtonClickedWithComment(rate: Int, comment: String)

    fun onPositiveButtonClickedWithoutComment(rate: Int)

    fun onNegativeButtonClicked()

    fun onNeutralButtonClicked()

    fun onNoneChoose()
}