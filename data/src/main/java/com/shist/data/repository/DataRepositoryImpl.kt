package com.shist.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.shist.data.model.BuildingItemImageJson
import com.shist.data.model.BuildingItemJson
import com.shist.data.repository.mappers.AddressItemDBMapper
import com.shist.data.repository.mappers.BuildingItemDBMapper
import com.shist.data.repository.mappers.IconItemDBMapper
import com.shist.data.repository.mappers.StructuralObjectItemDBMapper
import com.shist.data.retrofit.MapDataApi
import com.shist.data.roomDB.BuildingItemsDatabase
import com.shist.data.roomDB.entities.buildingItem.BuildingItemDB
import com.shist.data.roomDB.entities.buildingItem.BuildingItemJsonMapper
import com.shist.data.roomDB.entities.buildingItem.adressItem.AddressItemJsonMapper
import com.shist.data.roomDB.entities.buildingItem.structuralObjectItem.StructuralObjectItemJsonMapper
import com.shist.data.roomDB.entities.buildingItem.structuralObjectItem.iconItem.IconItemJsonMapper
import com.shist.domain.BuildingItem
import com.shist.domain.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

// This is implementation of repository based on its interface
class DataRepositoryImpl(private val buildingItemsDatabase: BuildingItemsDatabase,
                         private val service: MapDataApi,
                         private val buildingItemJsonMapper: BuildingItemJsonMapper,
                         private val buildingItemDBMapper: BuildingItemDBMapper,
                         private val structuralObjectItemJsonMapper: StructuralObjectItemJsonMapper,
                         private val structuralObjectItemDBMapper: StructuralObjectItemDBMapper,
                         private val addressItemJsonMapper: AddressItemJsonMapper,
                         private val addressItemDBMapper: AddressItemDBMapper,
                         private val iconItemJsonMapper: IconItemJsonMapper,
                         private val iconItemDBMapper: IconItemDBMapper,
                         private val context: Context
) : DataRepository {

    // This function checks if given item has ID or not
    private fun isItemWithID(itemJson: BuildingItemJson?): Boolean {
        return if (itemJson == null) {
            false
        } else {
            itemJson.id != null
        }
    }

    // This function checks if given item is empty or not
    private fun isItemNotEmpty(item: BuildingItemDB?): Boolean {
        return if (item == null)
            false
        else {
            item.buildingItemEntityDB.inventoryUsrreNumber != null ||
                    item.buildingItemEntityDB.name != null ||
                    item.buildingItemEntityDB.isModern != null ||
                    item.buildingItemEntityDB.type != null ||
                    item.buildingItemEntityDB.markerPath != null
        }
    }

    // This function is needed to get actual data from server
    override suspend fun loadData() {
        try {
            val jsonFile = File("./data.json")

            // Read the contents of the file into a string
            val jsonString = /*jsonFile.readText()*/ context.assets.open("data.json").bufferedReader().use { it.readText() }

            val generatedList = Gson().fromJson(jsonString, Array<BuildingItemJson>::class.java).toList()
            Log.d("EGOR", "0")
            val items = service.getData()/*generatedList*/
                ?.filter { isItemWithID(it) } // Clean list from items with null id
                ?.map {
                    Log.d("EGOR", "1")
                    val itemsImages : List<BuildingItemImageJson> =
                        service.getImagesWithBuildingId(it.id!!)/*emptyList()*/
                    Log.d("EGOR", "2")
                    BuildingItemJsonMapper().fromJsonToRoomDB(it, itemsImages)!!
                }.also { Log.d("EGOR", "3") }
                ?.filter { isItemNotEmpty(it) } // Clean DB from items with empty data
            buildingItemsDatabase.buildingItemsDao().insertBuildingItemsList(items!!)
            Log.d("EGOR", "4")
        } catch (e: Throwable) {
            Log.d("EGOR", "5: $e")
            throw NullPointerException("Error: " +
                    "Some BuildingItem (or even whole list) from json is empty!\n" + e.message)
        }
    }

    // This function return list of all items that are in local database
    override fun getItems(): Flow<List<BuildingItem>> {
        try {
            return buildingItemsDatabase.buildingItemsDao().getAllBuildingItems().map { list ->
                list.map { BuildingItemDBMapper().fromDBToDomain(it)!! } }
        } catch (e: Throwable) {
            throw NullPointerException("Error while getting building items: " +
            "Some item is nullable!\n" + e.message)
        }
    }

}