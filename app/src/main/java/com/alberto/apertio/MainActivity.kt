package com.alberto.apertio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var trabajo_cnta: Job
    private lateinit var trabajo_apertura: Job
    private lateinit var vibe: Vibrator

    var latitude:Double= 0.0
    var longitude:Double = 0.0
    var altitude:Double = 0.0

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 123

    }


    private fun removeLocationUpdates() {

        locationManager.removeUpdates(locationListener)

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var tv_coord:TextView = findViewById(R.id.tv_coord)
        var interruptor_gps: Switch = findViewById(R.id.sw_gps)


        interruptor_gps.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                if(isChecked){

                    interruptor_gps.text="Abrir por ubicación (Activado)"

                }else{

                    interruptor_gps.text="Abrir por ubicación (Desactivado)"
                }

            }
        })




        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {

            override fun onLocationChanged(location: Location) {

                latitude = location.latitude
                longitude = location.longitude
                altitude = location.altitude

                tv_coord.text = latitude.toString() + ", " + longitude.toString()

                if(latitude < 40.2334405484 && latitude > 40.23340000 && longitude < -3.7624579668 && longitude > -3762450000){

                    if(interruptor_gps.isChecked){

                        trabajo_apertura=GlobalScope.launch(Dispatchers.IO){

                            abrir_puerta("http://redalberto.ddns.net:1811/pulsar",0)
                            trabajo_apertura.cancel()

                        }


                    }


                }
            }

            override fun onProviderDisabled(provider: String) {


            }

            override fun onProviderEnabled(provider: String) {

            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {


            }
        }

    }

    override fun onResume() {

        super.onResume()
        requestLocationUpdates()

    }

    override fun onPause() {
        super.onPause()
        removeLocationUpdates()
    }

    private fun requestLocationUpdates() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

        }

        val skbar:SeekBar = findViewById(R.id.seekBar2)
        val tv_valor_cuenta: TextView = findViewById(R.id.tv_valor_cuenta)
        val btnAbrir: AppCompatButton= findViewById(R.id.btnAbrir)
        var btnParar: AppCompatButton = findViewById(R.id.btnParar)
        val fundido = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val rotar:Animation = AnimationUtils.loadAnimation(this,R.anim.rotar)
        val agitar:Animation = AnimationUtils.loadAnimation(this,R.anim.temblar)
        vibe = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        manejar_btn(btnAbrir,R.drawable.verde,R.drawable.verde_pulsado,34.0f,32.0f,vibe)
        manejar_btn(btnParar,R.drawable.rojo,R.drawable.rojo_pulsado,20.0f,18.0f,vibe)



        btnAbrir.setOnClickListener{

            trabajo_cnta=GlobalScope.launch(Dispatchers.Main){

                btnAbrir.isEnabled=false
                btnParar.isEnabled=true
                iniciar_temporizador(skbar)
                btnAbrir.isEnabled=true
                btnAbrir.startAnimation(agitar)
                btnParar.isEnabled=false

            }

            trabajo_apertura=GlobalScope.launch(Dispatchers.IO){

                abrir_puerta("http://redalberto.ddns.net:1811/pulsar",skbar.progress.toLong()*1000)
            }

        }

        btnParar.setOnClickListener{

            trabajo_cnta.cancel()
            trabajo_apertura.cancel()

            btnAbrir.isEnabled=true
            btnParar.isEnabled=false
            btnParar.startAnimation(fundido)

        }

        skbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {


                tv_valor_cuenta.text = skbar.progress.toString() + "\""
                if(skbar.progress !=0){
                   btnAbrir.setText("Iniciar")
                    btnParar.visibility= View.VISIBLE
                }else{
                    btnAbrir.setText("Abrir")
                    btnParar.visibility=View.INVISIBLE
                    btnParar.isEnabled=false


                }
                vibe.vibrate(18)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Perform any actions when the user starts interacting with the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Perform any actions when the user stops interacting with the SeekBar
            }
        })

    }


    fun manejar_btn(btn:AppCompatButton,drw:Int,drw_puls:Int,fntsizup:Float,fntsizdwn:Float,vibe:Vibrator){

        btn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    vibe.vibrate(50)
                    btn.setBackgroundResource(drw_puls)
                    btn.setTextSize(fntsizdwn)
                }
                MotionEvent.ACTION_UP -> {
                    vibe.vibrate(50)
                    btn.setBackgroundResource(drw)
                    btn.setTextSize(fntsizup)
                }
            }
            false // Return false to allow the event to be handled by other listeners
        }

    }

    suspend fun abrir_puerta(url:String,retardo:Long){

        val url = URL(url)
        delay(retardo)


        try{

            val conex = url.openConnection() as HttpURLConnection

            conex.requestMethod = "GET"

            val respuesta = conex.responseCode

            vibe.vibrate(3000)
            conex.disconnect()

        }catch(excp:Exception){

            excp.printStackTrace()

        }

    }

    suspend fun iniciar_temporizador(skbar:SeekBar){

            while(skbar.progress>0){

                if(skbar.progress<5){

                    vibe.vibrate(250)
                }
                delay(1000)
                skbar.progress--

            }
    }

}