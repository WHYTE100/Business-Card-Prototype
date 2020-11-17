package com.pridetechnologies.businesscard.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pridetechnologies.businesscard.R
import com.pridetechnologies.businesscard.models.Card
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CardAdapter(internal var context: Context,
                    internal var data:List<Card>
): RecyclerView.Adapter<CardAdapter.ViewPagerViewHolder>()  {
    inner class ViewPagerViewHolder(itemView:View): RecyclerView.ViewHolder(itemView)

    private var card_contact_image: CircleImageView? = null
    private var card_contact_name: TextView? = null
    private var card_contact_email: TextView? = null
    private var card_contact_mobile: Chip? = null

    internal var layoutInflater:LayoutInflater

    init {
        layoutInflater = LayoutInflater.from(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val view = layoutInflater.inflate(R.layout.card_screen_slide_page,parent, false)

        card_contact_image = view.findViewById(R.id.circleImageView) as CircleImageView
        card_contact_name = view.findViewById(R.id.textView) as TextView
        card_contact_email = view.findViewById(R.id.textView2) as TextView
        return ViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {

        Picasso.get().load(data[position].user_image).into(card_contact_image)
        card_contact_name!!.text = data[position].user_first_name
        card_contact_email!!.text = data[position].user_email
        card_contact_mobile!!.text = data[position].user_email

        card_contact_mobile!!.setOnClickListener{
            Toast.makeText(context, data[position].user_email,
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }


}


/*
* internal var layoutInflater:LayoutInflater

    init {
        layoutInflater = LayoutInflater.from(context)
    }
    override fun isViewFromObject(p0: View, `object`: Any): Boolean {
        return p0==`object`
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(R.layout.fragment_screen_slide_page,null, false)

        val card_contact_image = view.findViewById(R.id.circleImageView) as CircleImageView
        val card_contact_name = view.findViewById(R.id.textView) as TextView
        val card_contact_email = view.findViewById(R.id.textView2) as TextView
        val card_contact_mobile = view.findViewById(R.id.chip4) as Chip

        Picasso.get().load(data[position].user_image).into(card_contact_image)
        card_contact_name.text = data[position].user_name
        card_contact_email.text = data[position].user_email
        card_contact_mobile.text = data[position].user_email

        card_contact_mobile.setOnClickListener{
            Toast.makeText(context, data[position].user_email,
                Toast.LENGTH_SHORT).show()
        }

        container.addView(view)
        return view
    }*/
