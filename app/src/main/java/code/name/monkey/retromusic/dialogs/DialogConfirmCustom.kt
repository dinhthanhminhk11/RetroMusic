package code.name.monkey.retromusic.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.LayoutDialogConfirmBinding

class DialogConfirmCustom(
    context: Context,
    private val content: String?,
    private val textConfirm: String?,
    private val onLogoutClick: () -> Unit,
    private val onCancelClick: (() -> Unit)? = null
) : Dialog(context) {
    private lateinit var binding: LayoutDialogConfirmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding = LayoutDialogConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.content.text = content
        binding.confirmButton.text = textConfirm


        binding.close.setOnClickListener {
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            onCancelClick?.invoke()
            dismiss()
        }

        binding.confirmButton.setOnClickListener {
            onLogoutClick.invoke()
            dismiss()
        }

    }

    companion object {
        fun create(
            context: Context,
            textConfirm: String? = context.getString(R.string.out),
            content: String?,
            onLogoutClick: () -> Unit,
            onCancelClick: (() -> Unit)? = null
        ): DialogConfirmCustom {
            return DialogConfirmCustom(context, content, textConfirm, onLogoutClick, onCancelClick)
        }
    }
}