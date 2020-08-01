package com.sadcubeapps.casinolite

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Vibrator
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.sadcubeapps.casinolite.databinding.ActivityMainBinding
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private val c = charArrayOf('k', 'm', 'b', 't', 'q', 'Q', 's', 'S', 'o')


    private lateinit var mInterstitialAd: InterstitialAd

    private val sharedPrefFile = "casinoLitesp"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var activityMainBinding: ActivityMainBinding
    private var balance: Double = 500.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        MobileAds.initialize(this, getString(R.string.admob_app_id))

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.interstial_ad_id)

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mInterstitialAd.show()
                super.onAdLoaded()
            }
        }



        sharedPreferences = this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)

        val savedBalValue = sharedPreferences.getDouble("balance",500.0)

        balance = savedBalValue
        activityMainBinding.winX = "x0"
        activityMainBinding.winAmount = "You won 0"
        activityMainBinding.bet = ""

        //balance = 500.0
        updateBalance()

        activityMainBinding.playButton.setOnClickListener{playBet(activityMainBinding.betAmountEditText.text.toString())}

        activityMainBinding.IncreaseBet1Button.setOnClickListener{increaseBet(1)}
        activityMainBinding.IncreaseBet10Button.setOnClickListener{increaseBet(10)}
        activityMainBinding.IncreaseBet100Button.setOnClickListener{increaseBet(100)}

        activityMainBinding.IncreaseBet1KButton.setOnClickListener{increaseBet(1000)}
        activityMainBinding.IncreaseBet10KButton.setOnClickListener{increaseBet(10000)}
        activityMainBinding.IncreaseBet100KButton.setOnClickListener{increaseBet(100000)}


        activityMainBinding.IncreaseBet1MButton.setOnClickListener{increaseBet(1000000)}
        activityMainBinding.IncreaseBet10MButton.setOnClickListener{increaseBet(10000000)}
        activityMainBinding.IncreaseBet100MButton.setOnClickListener{increaseBet(100000000)}

        activityMainBinding.clrBetButton.setOnClickListener { clearBet() }


    }


    override fun onSaveInstanceState(outState: Bundle) {
        increaseBalance(0.0)

        outState.putDouble("balance", balance)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        balance = savedInstanceState.getDouble("balance")
        updateBalance()
    }

    @SuppressLint("SetTextI18n")
    private fun playBet(amount: String) {
        if(amount.trim().isEmpty()) {
            Toast.makeText(this, "Enter Bet", Toast.LENGTH_SHORT).show()
            vibrate(300)
        }
        else {
            val bet: Long = amount.trim().toLong()
            if(bet > balance) {
                Toast.makeText(this, "No balance", Toast.LENGTH_SHORT).show()
                vibrate(300)
            }
            else {
                val chances = listOf(0.0, 0.0, 0.1, 0.1, 0.2, 0.2, 0.3, 0.3, 0.5, 0.7, 1.0, 1.0, 1.5, 2.0, 2.5, 3.0, 5.0)
                val win: Double = chances.shuffled().take(1)[0]
                val winAmount = bet.toDouble()*win

                activityMainBinding.winX = "x${win}"
                if(win<1.0) {
                    //activityMainBinding.winAmountTextView.text = "You lost ${coolFormat(bet - winAmount, 0)}"
                    activityMainBinding.winAmount = "You lost ${coolFormat(bet - winAmount, 0)}"
                }
                else if(win>1.0) {
                    activityMainBinding.winAmount = "You won ${coolFormat(winAmount - bet, 0)}"
                }
                else {
                    activityMainBinding.winAmount = "You saved ${coolFormat(bet.toDouble(), 0)}"
                }

                increaseBalance(winAmount-bet)
                updateBalance()

                if(win==chances.max()) {
                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                    if(mInterstitialAd.isLoaded) {
                        mInterstitialAd.show()
                    }
                }
            }
        }
    }

    private fun increaseBet(amount: Long) {
        if(activityMainBinding.betAmountEditText.text.toString().trim().isEmpty()) {
            activityMainBinding.bet = amount.toString()
        }
        else {
            activityMainBinding.bet = (activityMainBinding.betAmountEditText.text.toString().toLong() + amount).toString()
        }

        updateBalance()
    }

    private fun increaseBalance(amount: Double) {
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()

        balance = coolDouble(balance+amount)
        if(balance.toLong()>=9000000000000000000) {
            Toast.makeText(this, "YOU WON THIS GAME!!!!", Toast.LENGTH_LONG).show()
            balance = coolDouble(500.0)
            vibrate(1000)
        }
        else if(balance.toLong()<=100) {
            val goodToast = Toast.makeText(this, "Extra money", Toast.LENGTH_LONG)
            balance = coolDouble(balance+250)
            vibrate(300)
            goodToast.show()
        }

        updateBalance()

        editor.putDouble("balance", balance)
        editor.apply()
        editor.commit()
        //Log.i("sp", sharedPreferences.getDouble("balance",500.0).toString())


    }

    private fun updateBalance() {

        activityMainBinding.balance = coolFormat(balance, 0)
    }

    private fun clearBet() {
        activityMainBinding.bet = ""
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    fun vibrate(duration: Int) {
        val vibs = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibs.vibrate(duration.toLong())
    }

    fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
        putLong(key, java.lang.Double.doubleToRawLongBits(double))

    fun SharedPreferences.getDouble(key: String, default: Double) =
        java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))

    fun coolDouble(x: Double): Double {
        return Math.floor(x * 1) / 1;
    }

    private fun coolFormat(n: Double, iteration: Int): String? {
        val d = n.toLong() / 100 / 10.0
        val isRound =
            d * 10 % 10 == 0.0 //true if the decimal part is equal to 0 (then it's trimmed anyway)
        return if (d < 1000) //this determines the class, i.e. 'k', 'm' etc
            (if (d > 99.9 || isRound || !isRound && d > 9.99) //this decides whether to trim the decimals
                d.toInt() * 10 / 10 else d.toString() + "" // (int) d * 10 / 10 drops the decimal
                    ).toString() + "" + c[iteration] else coolFormat(d, iteration + 1)
    }

}