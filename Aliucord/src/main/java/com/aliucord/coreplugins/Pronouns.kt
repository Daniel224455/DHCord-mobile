package com.dhcord.coreplugins

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.dhcord.Constants
import com.dhcord.Utils
import com.dhcord.api.rn.user.RNUserProfile
import com.dhcord.entities.Plugin
import com.dhcord.patcher.after
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.lytefast.flexinput.R

val sheetProfileHeaderViewId = Utils.getResId("user_sheet_profile_header_view", "id")
val userProfileHeaderSecondaryNameViewId = Utils.getResId("user_profile_header_secondary_name", "id")

val pronounsViewId = View.generateViewId()

internal class Pronouns : Plugin(Manifest("Pronouns")) {
    override fun load(context: Context) {
        patcher.after<UserProfileHeaderView>("configureSecondaryName", UserProfileHeaderViewModel.ViewState.Loaded::class.java) {
            if (id != sheetProfileHeaderViewId) return@after
            val state = it.args[0] as? UserProfileHeaderViewModel.ViewState.Loaded ?: return@after

            val profile = state.userProfile as? RNUserProfile ?: return@after
            val pronouns = profile.guildMemberProfile?.pronouns?.ifEmpty { null }
                ?: profile.userProfile?.pronouns?.ifEmpty { null }
                ?: return@after

            val secondaryNameView = findViewById<SimpleDraweeSpanTextView>(userProfileHeaderSecondaryNameViewId)
            val layout = secondaryNameView.parent as LinearLayout

            layout.findViewById(pronounsViewId) ?: TextView(layout.context, null, 0, R.i.UiKit_TextView_Semibold).apply {
                id = pronounsViewId
                typeface = ResourcesCompat.getFont(layout.context, Constants.Fonts.whitney_semibold)
                setTextColor(secondaryNameView.currentTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, secondaryNameView.textSize)
                text = pronouns

                layout.addView(this, layout.indexOfChild(secondaryNameView) + 1)
            }
        }
    }

    override fun start(context: Context?) {}

    override fun stop(context: Context?) {}
}
