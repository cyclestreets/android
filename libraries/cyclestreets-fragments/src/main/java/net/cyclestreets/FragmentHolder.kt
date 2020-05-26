package net.cyclestreets

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


abstract class FragmentHolder : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().add(android.R.id.content, fragment()).commit()
    }

    protected abstract fun fragment(): Fragment
}