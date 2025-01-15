package com.androidace.echojournal.di

import com.androidace.echojournal.ui.common.UIStateHandlerImpl
import com.androidace.echojournal.ui.common.UiStateHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule{
    @Binds
    abstract fun provideUiStateHandle(impl: UIStateHandlerImpl): UiStateHandler
}