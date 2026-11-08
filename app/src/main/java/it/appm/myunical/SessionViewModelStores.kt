package it.appm.myunical

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import it.fra.myunical.domain.model.auth.AuthState

/**
 * One ViewModel store per authentication state, kept across configuration changes. When the state
 * changes the stores of the other states are cleared: after a logout the login starts empty (instead
 * of resuming the loading state of the previous sign-in) and a new sign-in gets fresh screens.
 */
class SessionViewModelStores : ViewModel() {
    private val stores = mutableMapOf<AuthState, ViewModelStore>()

    fun storeFor(state: AuthState): ViewModelStore {
        stores.keys.filter { it != state }.forEach { stores.remove(it)?.clear() }
        return stores.getOrPut(state) { ViewModelStore() }
    }

    override fun onCleared() {
        stores.values.forEach(ViewModelStore::clear)
        stores.clear()
    }
}

/** Owner of a session store that creates ViewModels like the activity does. */
class SessionViewModelStoreOwner(
    override val viewModelStore: ViewModelStore,
    private val parent: HasDefaultViewModelProviderFactory
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = parent.defaultViewModelProviderFactory

    override val defaultViewModelCreationExtras: CreationExtras
        get() = MutableCreationExtras(parent.defaultViewModelCreationExtras).apply {
            set(VIEW_MODEL_STORE_OWNER_KEY, this@SessionViewModelStoreOwner)
        }
}
