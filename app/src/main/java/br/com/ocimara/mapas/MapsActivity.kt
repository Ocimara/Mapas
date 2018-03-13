package br.com.ocimara.mapas

import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import android.Manifest
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log



class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
                        GoogleApiClient.ConnectionCallbacks,
                        GoogleApiClient.OnConnectionFailedListener  {

    val REQUEST_GPS = 212

    // METODOS PARA LIDAR COM GPS
    override fun onConnected(p0: Bundle?) {
      //conseguiu conexao da localização, porém o usuario tem que permitir usar

        checkPermission()

        val minhaLocalizacao = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient)

        if (minhaLocalizacao != null)
        adicionarMarcador(minhaLocalizacao.latitude, minhaLocalizacao.longitude, "Aqui estou eu!!!!")


    }

    private fun checkPermission() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("", "Permissão para gravar negada")

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                val builder = AlertDialog.Builder(this)

                builder.setMessage("Necessária a permissao para GPS")
                        .setTitle("Permissao Requerida")

                builder.setPositiveButton("OK") { dialog, id ->
                    requestPermission()
                }

                val dialog = builder.create()
                dialog.show()

            } else {
                requestPermission()
            }
        }
    }

    protected fun requestPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_GPS)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_GPS -> {
                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permissão negada pelo usuário")
                } else {
                    Log.i("TAG", "Permissao concedida pelo usuario")
                }
                return
            }
        }
    }


    override fun onConnectionSuspended(p0: Int) {
        Log.i("TAG", "QUANDO O APP ESTÁ SUSPENSO")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("TAG", "ERRO DE CONEXÃO")
    }

    //nomentatura começa com m quando é variavel local
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient


    //Google recomenda colocar o sybchronized

    @Synchronized fun callConnection(){
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient.connect()


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btPesquisar.setOnClickListener{

            mMap.clear()

            val geocoder = Geocoder(this)
            var address : List<Address>?

            address = geocoder
                    .getFromLocationName(
                            etEndereco.text.toString(),
                            1)
            if (address.isNotEmpty()) {
                val location = address[0]
                adicionarMarcador(location.latitude, location.longitude, "Endereço Pesquisado")
            }
            else
            {
                /**
                 * snackbar tb dá para apesentar msg (ela sobre de baixo para cima), ele pode ter
                 * ações, para funcionar tem que passar coordinator layout, dá para customizar o layout
                 * toast apresenta a msg de baixo para cima e não apresenta ação
                 */



                var alert = AlertDialog.Builder(this).create()
                alert.setTitle("Ops!!! Deu ruim!!")
                alert.setMessage("Endereço não encontrado!")
                alert.setCancelable(false) //trava a tela até quo o ususaio clique no botão

                alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", {
                    dialogInterface,inteiro ->
                    etEndereco.text.clear()
                    alert.dismiss()
                })

                alert.show()
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        callConnection()

        // verifica se o mapa está startado
        // Sets the map type to be "hybrid"
        //mMap.setMapType(googleMap.mapType(1))

        // Set a preference for minimum and maximum zoom.
        //mMap.setMinZoomPreference(18.0f)
        //mMap.setMaxZoomPreference(34.0f)
        // Add a marker in Sydney and move the camera
        //-23.517539, -46.752099
        //val sydney = LatLng(-23.5187662, -46.7523963)

    }

    fun adicionarMarcador(lat: Double, long: Double, title: String )
    {
        val sydney = LatLng(lat, long)
        mMap.addMarker(MarkerOptions()
                .position(sydney)
                .title(title)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.mipmap.ic_launcher))
                .draggable(true) //deixar arrastar
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16f))

    }

}

/**
 * Anchor
O ponto da imagem que será colocado na posição LatLng do marcador. Por padrão, é o meio da parte inferior da imagem.
Alpha
Define a opacidade do marcador. O valor padrão é 1.0.
Title
Uma string exibida na janela de informações quando o usuário toca o marcador.
Snippet
Texto adicional exibido abaixo do título.
Icon
Um bitmap exibido em vez da imagem padrão do marcador.
Draggable
Defina como true para permitir que o usuário mova o marcador. O valor padrão é false.
Visível
Defina como false para que o marcador fique invisível. O valor padrão é true.
 */
