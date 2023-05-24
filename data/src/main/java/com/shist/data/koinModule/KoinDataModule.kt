package com.shist.data.koinModule

import android.content.Context
import androidx.room.Room
import com.shist.data.repository.DataRepositoryImpl
import com.shist.data.repository.mappers.AddressItemDBMapper
import com.shist.data.repository.mappers.BuildingItemDBMapper
import com.shist.data.repository.mappers.IconItemDBMapper
import com.shist.data.repository.mappers.StructuralObjectItemDBMapper
import com.shist.data.retrofit.RetrofitClient
import com.shist.data.retrofit.MapDataApi
import com.shist.data.roomDB.BuildingItemsDatabase
import com.shist.data.roomDB.MigrationDB
import com.shist.data.roomDB.entities.buildingItem.BuildingItemJsonMapper
import com.shist.data.roomDB.entities.buildingItem.adressItem.AddressItemJsonMapper
import com.shist.data.roomDB.entities.buildingItem.structuralObjectItem.StructuralObjectItemJsonMapper
import com.shist.data.roomDB.entities.buildingItem.structuralObjectItem.iconItem.IconItemJsonMapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.shist.domain.DataRepository

// Koin module is needed for initializing all variables and objects in classes
val dataModule = module {

    single<DataRepository> {
        DataRepositoryImpl(
            buildingItemsDatabase = get(), service = get(),
            buildingItemJsonMapper = get(), buildingItemDBMapper = get(),
            structuralObjectItemJsonMapper = get(), structuralObjectItemDBMapper = get(),
            addressItemJsonMapper = get(), addressItemDBMapper = get(),
            iconItemJsonMapper = get(), iconItemDBMapper = get(), context = androidContext()
        )
    }

    single {
        fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                BuildingItemsDatabase::class.java, "bsumapDB"
            ) // Use code below and your Migration if you've made some changes in DB structure
                .addMigrations(MigrationDB.MIGRATION_1_2)
                //.addMigrations(MigrationDB.MIGRATION_2_3)
                .build()

        buildDatabase(androidContext())
    }

    single {
        val retrofitClient: RetrofitClient = get()

        retrofitClient.retrofit("https://331a-46-53-254-157.ngrok-free.app/")
            .create(MapDataApi::class.java)
    }

    single { RetrofitClient(client = get()) }

    single {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    single { BuildingItemJsonMapper() }

    single { BuildingItemDBMapper() }

    single { StructuralObjectItemJsonMapper() }

    single { StructuralObjectItemDBMapper() }

    single { AddressItemJsonMapper() }

    single { AddressItemDBMapper() }

    single { IconItemJsonMapper() }

    single { IconItemDBMapper() }

}