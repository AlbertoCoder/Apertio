package com.alberto.apertio

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var trabajo_cnta:Job
    private lateinit var trabajo_apertura:Job
    private lateinit var vibe: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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