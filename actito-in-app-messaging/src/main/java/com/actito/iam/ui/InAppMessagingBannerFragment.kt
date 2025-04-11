package com.actito.iam.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.actito.iam.R
import com.actito.iam.databinding.ActitoInAppMessageBannerFragmentBinding
import com.actito.iam.internal.caching.ActitoImageCache
import com.actito.iam.models.ActitoInAppMessage
import com.actito.iam.ui.base.InAppMessagingBaseFragment

public open class InAppMessagingBannerFragment : InAppMessagingBaseFragment() {
    private lateinit var binding: ActitoInAppMessageBannerFragmentBinding

    override val animatedView: View
        get() = binding.cardView

    override val enterAnimation: Int = R.anim.actito_iam_banner_enter_animation
    override val exitAnimation: Int = R.anim.actito_iam_banner_exit_animation

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActitoInAppMessageBannerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val image = ActitoImageCache.getOrientationConstrainedImage(requireContext())

        binding.imageView.isVisible = image != null
        binding.imageView.setImageBitmap(image)

        binding.titleView.isVisible = !message.title.isNullOrBlank()
        binding.titleView.text = message.title

        binding.messageView.isVisible = !message.message.isNullOrBlank()
        binding.messageView.text = message.message

        binding.cardView.setOnClickListener {
            handleActionClicked(ActitoInAppMessage.ActionType.PRIMARY)
        }

        binding.root.setOnClickListener {
            dismiss()
        }

        if (savedInstanceState == null) {
            animate(Transition.ENTER)
        }
    }
}
