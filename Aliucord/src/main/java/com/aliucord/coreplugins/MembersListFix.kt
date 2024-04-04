/*
 * This file is part of dhcord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.dhcord.coreplugins

import android.content.Context
import com.dhcord.entities.Plugin
import com.dhcord.patcher.Hook
import com.dhcord.patcher.Patcher
import com.discord.utilities.lazy.memberlist.ChannelMemberList
import com.discord.utilities.lazy.memberlist.MemberListRow

internal class MembersListFix : Plugin(Manifest("MembersListFix")) {
    @Suppress("UNCHECKED_CAST")
    override fun load(context: Context?) {
        val groups = ChannelMemberList::class.java.getDeclaredField("groups").apply { isAccessible = true }
        Patcher.addPatch(ChannelMemberList::class.java.getDeclaredMethod("setGroups", List::class.java, Function1::class.java), Hook {
            val list = it.thisObject as ChannelMemberList
            val rows = list.rows
            val groupsMap = groups[list] as Map<String, MemberListRow>
            list.groupIndices.forEach { (idx, id) -> rows[idx] = groupsMap[id] }
        })
    }

    override fun start(context: Context?) {}
    override fun stop(context: Context?) {}
}
