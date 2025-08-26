package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityCarDetailBinding
import com.example.myapitest.model.Car
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.ui.loadUrl

class CarDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var car: Car
    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var mMap: GoogleMap
    private lateinit var idCar: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupView()
        setupGoogleMap()
        loadCar()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if(::car.isInitialized){
            //Se o item já foi carregado, carrega a localização no mapa
            loadItemLocationInGoogleMap()
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener{
            finish()
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.googleMapContent, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)
    }

    private fun loadCar(){
        idCar = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.getCarById(idCar)
            }

            withContext(Dispatchers.Main){
                when(result) {
                    is Result.Success -> {
                        car = result.data.value
                        handleSuccess()
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            getString(R.string.erro_get),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun handleSuccess() {
        binding.name.text = car.name
        binding.year.text = car.year
        binding.licence.setText(car.licence)
        if (car.imageUrl.isNotEmpty()) {
            binding.image.loadUrl(car.imageUrl)
        }
    }

    private fun loadItemLocationInGoogleMap() {
        car.place.apply {
            binding.googleMapContent.visibility = View.VISIBLE
            val latLong = LatLng(lat, long)
            mMap.addMarker(
                MarkerOptions()
                    .position(latLong)
                    .title(getString(R.string.esta_aqui))
            )
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(latLong, 15f)
            )
        }
    }

    companion object {

        private const val ARG_ID = "arg_id"

        fun newIntent(
            context: Context,
            itemId: String
        ) = Intent(context, CarDetailActivity::class.java).apply {
            putExtra(ARG_ID, itemId)
        }
    }
}