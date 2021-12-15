MVVM is a structural design pattern and its based on the separation of the project structure into 3 main components:
The Model, which is responsible for representing and holding the application data.
The View, which is responsible to manage UI components.
The ViewModel, which is responsible for handling the data received from the Model and pass them to the View.
The main advantages of this structure are:
Separation of concerns.
You divide responsibilities clearly between different components.
By doing this you’ll obtain a more maintainable project structure.
Ease of testing.
By splitting the code into small units, it will be easier to write comprehensive unit tests.
As Google explains in its official documentation, the MVVM pattern is the suggested architectural pattern for Android applications.


This simple project will consist of an endless cat fact generator.
This app will use the Cat Facts API to retrieve random facts about cats.
You can view the complete project at this GitHub repo.

Required libraries
For this project, you will need to use these libraries:
Hilt
It will be responsible for automatically manage the dependencies through your application.
You can read more about Hilt on Android in the official documentation.
Retrofit
An HTTP client that will handle the interaction with the API.
To parse the JSON response received from the API into the declared model, we will use the Gson converter.
Project setup
Let’s start by setting up the required dependencies and permissions.
First, let’s declare the required INTERNET permission in your AndroidManifest.xml file:

<uses-permission android:name="android.permission.INTERNET" />
Then, setup the Hilt required dependencies in your project-level build.gradle file:
classpath 'com.google.dagger:hilt-android-gradle-plugin:2.38.1'
Finally, declare the dependencies in your application level build.gradle file:
plugins {
    ...
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
}
android {
    ...
    // Enable view binding
    buildFeatures {
        viewBinding true
    }
}
dependencies {
    ...
    // Lifecycle
    implementation 'androidx.fragment:fragment-ktx:1.3.6'
    // Hilt
    implementation "com.google.dagger:hilt-android:2.38.1"
    kapt "com.google.dagger:hilt-compiler:2.38.1"

    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0' 
    // Retrofit Gson converter
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
}
Once you have set up and synched your dependencies you are ready to pass to the implementation phase.

Project implementation
There should be 4 packages under main app package
1. The di the directory will contain the classes responsible for dependency injection.
2. The model the directory will contain the classes which represent the data structures.
3. The service directory represents the classes responsible for interacting with the API.
4. The view directory will contain all files related to the UI.
   
In the MVVM architecture, every component depends on one of the levels below.

1. Model declaration
   Let’s start with the model.
   This is a representation of the data received from the API.
   You need to represent this model in your application.
   Create a new CatFact.kt class in your model folder and specify its properties:
   data class CatFact (
   @SerializedName("fact")
   @Expose
   var fact: String,

   @SerializedName("length")
   @Expose
   var length: Int
   )
   Note that I used two annotations on each attribute:
   SerializedName indicates that the property should be serialized with the specified name.
   Expose indicates that the property should be exposed for JSON serialization and deserialization.
   This will be used by the Gson converter at the moment of decoding and encoding data.
2. Declaring Hilt base Application
   Let’s build the basis for automatic dependency injection.
   Create a new class annotated with HiltAndroidApp in your application main folder.
   @HiltAndroidApp
   class CatFactApplication : Application()
   This will serve as the parent container for all injected dependencies.
   After creating the class you need to specify it in your AndroidManifest.xml file.
   <application
   ...
   android:name=".CatFactApplication">
   ...
   </application>
3. Implement your Retrofit service
   You can now pass on the implementation of your Retrofit service.
   This will be responsible for sending the HTTP GET request to the API.
   First, create a new interface in your service/api folder.
   interface CatFactService {
   @GET("fact")
   suspend fun getCatFact(): CatFact
   }
   the interface contains one asynchronous method.
   This method will retrieve a random CatFact after executing a GET request to the fact path.
   Second, implement a Hilt module.
   This module will provide all network-related dependencies globally, through your application.
   Create a file called ApiModule in the di folder:
   @Module
   @InstallIn(SingletonComponent::class)
   object ApiModule {

   @Singleton
   @Provides
   fun provideGsonBuilder(): Gson =
   GsonBuilder()
   .excludeFieldsWithoutExposeAnnotation()
   .create()

   @Singleton
   @Provides
   fun provideRetrofit(gson: Gson): Retrofit.Builder =
   Retrofit.Builder()
   .baseUrl("https://catfact.ninja/")
   .addConverterFactory(GsonConverterFactory.create(gson))

   @Singleton
   @Provides
   fun provideCatFactService(retrofit: Retrofit.Builder): CatFactService =
   retrofit
   .build()
   .create(CatFactService::class.java)
   }
   The object is annotated with Module and InstallIn.
   The InstallIn destination is the SingletonComponent::class.
   Hilt will use this information to automatically make available these dependencies globally.
   As you can see, there are 3 functions:
   1. A function is responsible for providing the Gson service.
   2. A function is responsible for providing the Retrofit service.
   3. A function responsible for providing the CatFactService declared before.
   The Singleton annotation guarantees that only one instance of each handler will be created.
4. Implement the repository components
   Now, it is time to implement the repository-level code.
   These classes will be responsible for handling the communications between the services and the view model.
   Let’s start by creating a CatFactRepository class in the service/repository folder:
   class CatFactRepository(
   private val catFactService: CatFactService
   ) {
   suspend fun getCatFact(): CatFact = catFactService.getCatFact()
   }
   its goal is to abstract the data sources from the rest of the app.
   After the repository has been created, you need to tell Hilt how to automatically provide it through your application.
   To do that create a RepositoryModule object in the di folder:
   @Module
   @InstallIn(SingletonComponent::class)
   object RepositoryModule {

   @Singleton
   @Provides
   fun provideCatFactRepository(
   catFactService: CatFactService
   ): CatFactRepository =
   CatFactRepository(catFactService)
   }
   This module is annotated as the ApiModule and will be responsible for providing the CatFactRepository instance in all your application.
5. Implement the ViewModel class
   It is now time to focus on the view model class.
   This class will be responsible for interacting with the repository and provide data to the view components.
   Create a new CatFactViewModel class in the view/viewmodel folder:
   @HiltViewModel
   class CatFactViewModel @Inject constructor(
   private val catFactRepository: CatFactRepository
   ) : ViewModel() {

   private val _catFact = MutableLiveData<CatFact>()
   val catFact: LiveData<CatFact> = _catFact

   fun loadCatFact() {
   viewModelScope.launch {
   try {
   _catFact.value = catFactRepository.getCatFact()
   } catch (e: Exception) {
   // Retrofit error
   e.printStackTrace()
   }
   }
   }
   }
   The class is annotated with HiltViewModel.
   Also, the Inject annotation makes Hilt automatically provide the required CatFactRepository instance on the constructor.
   This class contains:
    1. A reference to the last fetched CatFact.
   This data is stored in a private MutableLiveData variable, and made available through a public LiveData instance, referring to the private object.
    2. A function.
   This will use the injected repository reference to call the API and retrieve a new cat fact.

6. Implement the View
   The final step is to implement the view-related files.
    1. Create a new activity_cat_fact layout in the res/layout folder.
    2. CatFactActivity class in the view/ui folder
       
    The objectives of this class are:
       1. Instantiate the view and its components using view binding.
       2. Use the view model instance to retrieve data from the API and populate the views when the LiveData instance triggers the onChange method.
       Notice that I used an Observer to look for changes in the view model catFact variable.
       Every time a new fact is available, the TextView will be updated.

    


