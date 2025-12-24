package de.mindmarket.core.presentation.di

import de.mindmarket.core.presentation.util.DialogSheetScopedViewModel
import de.mindmarket.core.presentation.util.ScopedStoreRegistryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val corePresentationModule = module {
    viewModelOf(::ScopedStoreRegistryViewModel)
}