package com.android.QKSMS.interactor

import com.android.QKSMS.manager.ShortcutManager
import com.android.QKSMS.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkAllUnLocked @Inject constructor(
        private val messageRepo: MessageRepository,
        private val updateBadge: UpdateBadge,
        private val shortcutManager: ShortcutManager
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.just(Unit)
                .doOnNext { messageRepo.markAllUnlocked() }
                .doOnNext { shortcutManager.updateShortcuts() } // Update shortcuts
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge and widget
    }

}