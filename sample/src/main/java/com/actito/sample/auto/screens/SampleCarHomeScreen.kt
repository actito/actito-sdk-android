package com.actito.sample.auto.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.actito.sample.R

class SampleCarHomeScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()

        listBuilder.addItem(
            Row.Builder()
                .setTitle(carContext.getString(R.string.auto_home_notifications_title))
                .addText(carContext.getString(R.string.auto_home_notifications_description))
                .build(),
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle(carContext.getString(R.string.auto_home_inbox_title))
                .addText(carContext.getString(R.string.auto_home_inbox_description))
                .build(),
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle(carContext.getString(R.string.auto_home_device_title))
                .addText(carContext.getString(R.string.auto_home_device_description))
                .build(),
        )

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setHeader(
                Header.Builder()
                    .setTitle(carContext.getString(R.string.app_name))
                    .setStartHeaderAction(Action.APP_ICON)
                    .build(),
            )
            .build()
    }
}
