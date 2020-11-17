package com.pridetechnologies.businesscard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pridetechnologies.businesscard.`interface`.IFirebaseLoadDone
import com.pridetechnologies.businesscard.adapters.CardAdapter
import com.pridetechnologies.businesscard.models.Card
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.card_screen_slide_page.view.*
import java.util.*

class MainActivity : FragmentActivity(), IFirebaseLoadDone {

    //Firebase references
    private var mDatabase: FirebaseDatabase? = null

    //UI elements
    private var profilePic: CircleImageView? = null
    private var scanCode: ImageButton? = null

    private var pp: String? = null

    private var mAuth: FirebaseAuth? = null

    lateinit var adapter: CardAdapter
    lateinit var usersRef: DatabaseReference

    lateinit var iFirebaseLoadDone: IFirebaseLoadDone

    private lateinit var viewPager: RecyclerView
    private var cardAdapter: FirebaseRecyclerAdapter<Card, CardHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().getReference("Users")

        val mUser = mAuth!!.currentUser

        iFirebaseLoadDone = this

        viewPager = findViewById(R.id.viewPager)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        layoutManager.reverseLayout = false
        viewPager.setHasFixedSize(true)
        viewPager.layoutManager = layoutManager
        val snapHelper = PagerSnapHelper() // Or PagerSnapHelper
        snapHelper.attachToRecyclerView(viewPager)
        //viewPager.setPageTransformer(ZoomOutPageTransformer())

        profilePic = findViewById<View>(R.id.circleImageView2) as CircleImageView

        scanCode = findViewById<View>(R.id.imageView) as ImageButton
        usersRef.child(mUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    pp = snapshot.child("user_image").value as String

                    Picasso.get().load(pp).into(profilePic)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        loadCards()
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun loadCards() {

        val options = FirebaseRecyclerOptions.Builder<Card>()
            .setQuery(usersRef, Card::class.java)
            .setLifecycleOwner(this)
            .build()

        cardAdapter = object : FirebaseRecyclerAdapter<Card, CardHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
                return CardHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.card_screen_slide_page, parent, false)
                )
            }

            protected override fun onBindViewHolder(holder: CardHolder, position: Int, model: Card) {
                holder.bind(model)
            }

            override fun onDataChanged() {
                // Called each time there is a new data snapshot. You may want to use this method
                // to hide a loading spinner or check for the "no documents" state and update your UI.
                // ...
            }
        }
        viewPager.adapter = cardAdapter
    }
    override fun onCardLoadSuccess(cardList: List<Card>) {
        // The pager adapter, which provides the pages to the view pager widget.
        //adapter = CardAdapter(this, cardList)
        viewPager.adapter = cardAdapter
    }

    override fun onCardLoadFailed(message: String) {
        TODO("Not yet implemented")
    }




    class CardHolder(val customView: View, var card: Card? = null) : RecyclerView.ViewHolder(
        customView
    ) {

        fun bind(card: Card?) {
            with(card) {
                if (card != null) {
                    Picasso.get().load(card.user_image).into(customView.circleImageView)
                    customView.textView?.text = card.user_first_name
                    customView.textView2?.text = card.user_email
                }
            }
        }
    }

    fun scanCode(view: View) {
        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        Animatoo.animateZoom(this)
    }

    fun openUserProfileDialog(view: View) {
        // val intent = Intent(this@MainActivity, ProfileActivity::class.java)
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // startActivity(intent)
        //Animatoo.animateZoom(this)
        //ProfileFragment.newInstance(getString(R.string.welcome), getString(R.string.welcome)).show(supportFragmentManager, ProfileFragment.TAG)

    }

    fun signOutUser(view: View) {
        mAuth?.signOut()
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finishAffinity()
    }


}
/*private fun loadCards() {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            var cards: MutableList<Card> = ArrayList()
            override fun onCancelled(error: DatabaseError) {
                iFirebaseLoadDone.onCardLoadFailed(error.message)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (cardSnapshot in snapshot.children) {
                    val my_cards = cardSnapshot.getValue(Card::class.java)
                    cards.add(my_cards!!)
                    iFirebaseLoadDone.onCardLoadSuccess(cards)
                }
            }

        })

    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }
    */