package com.tunjid.androidx.core.testclasses

import android.app.Service
import android.content.Intent
import com.tunjid.androidx.core.components.services.SelfBinder
import com.tunjid.androidx.core.components.services.SelfBindingService

class TestService : Service(), SelfBindingService<TestService> {

    private val binder = TestServiceBinder()

    override fun onBind(intent: Intent): SelfBinder<TestService> = binder

    private inner class TestServiceBinder : SelfBinder<TestService>() {
        override val service: TestService
            get() = this@TestService
    }
}
