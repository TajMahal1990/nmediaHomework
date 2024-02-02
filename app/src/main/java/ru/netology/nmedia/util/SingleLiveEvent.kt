package ru.netology.nmedia.util

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {
//    // FIXME: упрощённый вариант, пока не прошли Atomic'и
//    private var pending = false

    private var pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        require (!hasActiveObservers()) {
            error("Multiple observers registered but only one will be notified of changes.")
        }

        super.observe(owner) {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
//            if (pending) {
//                pending = false
//                observer.onChanged(it)
//            }
        }
    }
    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
     //   pending = true
        super.setValue(t)
    }
}