package com.ridelineTeam.application.rideline.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zagum.switchicon.SwitchIconView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ridelineTeam.application.rideline.R;
import com.ridelineTeam.application.rideline.model.RideCost;
import com.ridelineTeam.application.rideline.util.enums.Cost;
import com.ridelineTeam.application.rideline.util.enums.Restrictions;
import com.ridelineTeam.application.rideline.util.files.ConstantsKt;
import com.ridelineTeam.application.rideline.util.helpers.ImageHelper;
import com.ridelineTeam.application.rideline.util.helpers.NotificationHelper;
import com.ridelineTeam.application.rideline.view.RideDetailActivity;
import com.ridelineTeam.application.rideline.util.helpers.DateTimeAndStringHelper;
import com.ridelineTeam.application.rideline.model.Ride;
import com.ridelineTeam.application.rideline.model.User;
import com.ridelineTeam.application.rideline.util.enums.Status;
import com.ridelineTeam.application.rideline.model.enums.Type;
import com.ridelineTeam.application.rideline.view.fragment.ProfileFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.ridelineTeam.application.rideline.R.drawable.*;
import static com.ridelineTeam.application.rideline.util.files.ConstantsKt.RIDES;
import static com.ridelineTeam.application.rideline.util.files.ConstantsKt.STATUS;
import static com.ridelineTeam.application.rideline.util.files.ConstantsKt.TOKEN;
import static com.ridelineTeam.application.rideline.util.files.ConstantsKt.USERS;


public class RideAdapter {

    static class RideViewHolder extends RecyclerView.ViewHolder {
        private TextView userCard;
        private TextView dateCard;
        private TextView typeCard;
        private TextView origin;
        private TextView destination;
        private TextView passengerCard;
        private ImageView rideImage;
        private CircleImageView userPicture;
        private Button btnGoCard;
        private Button btnCancelCard;
        private TextView hour;
        private FrameLayout frameLayoutCard;
        private SwitchIconView smokeIcon;
        private SwitchIconView petIcon;
        private SwitchIconView foodIcon;
        private SwitchIconView sleepIcon;
        private SwitchIconView childIcon;
        private TextView rideCost;

        private RideViewHolder(View view) {
            super(view);
            userCard = view.findViewById(R.id.userCard);
            dateCard = view.findViewById(R.id.dateCard);
            typeCard = view.findViewById(R.id.typeCard);
            passengerCard = view.findViewById(R.id.passengerCard);
            rideImage = view.findViewById(R.id.typeRideImage);
            userPicture = view.findViewById(R.id.userPicture);
            btnGoCard = view.findViewById(R.id.btnGoCard);
            btnCancelCard = view.findViewById(R.id.btnCancelCard);
            destination = view.findViewById(R.id.txtDestination);
            origin = view.findViewById(R.id.txtOrigin);
            hour = view.findViewById(R.id.rideHour);
            frameLayoutCard = view.findViewById(R.id.frameLayoutCard);
            smokeIcon = view.findViewById(R.id.smokeIcon);
            petIcon = view.findViewById(R.id.petIcon);
            foodIcon = view.findViewById(R.id.foodIcon);
            sleepIcon = view.findViewById(R.id.sleepIcon);
            childIcon = view.findViewById(R.id.childIcon);
           // rideCost=view.findViewById(R.id.ridePrice);
        }
    }


    public static class RideAdapterRecycler extends RecyclerView.Adapter<RideViewHolder> {
        private Context context;
        private DatabaseReference databaseReference;
        private ChildEventListener childEventListener;
        private List<Ride> Rides;
        private List<String> RidesIds;
        private String userId;
        private Activity activity;
        private String fullName;
        private TextView noRidesText;


        public RideAdapterRecycler(final Context context, DatabaseReference reference,
                                   Activity activity, Query query, ArrayList<String> communities, final String userId,
                                   TextView textView) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.Rides = new ArrayList<>();
            this.RidesIds = new ArrayList<>();
            this.userId = userId;
            this.noRidesText = textView;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault());

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildAdded " + dataSnapshot.getKey());
                    for (int i = 0; i < communities.size(); i++) {
                        if (communities.get(i).equals(dataSnapshot.child("community").getValue())) {
                            Ride ride = dataSnapshot.getValue(Ride.class);
                            String rideDate = null;
                            try {
                                if (ride != null) {
                                    rideDate = ride.getDate()+" "+ ride.getTime();
                                    rideDate = DateTimeAndStringHelper.dateFormat(rideDate);
                                }
                                Date dateRide = formatter.parse(rideDate);
                                Date currentDate = new Date();
                                if(dateRide.after(currentDate)){
                                    RidesIds.add(dataSnapshot.getKey());

                                    Rides.add(ride);

                                    notifyItemInserted(Rides.size() - 1);
                                }
                                else{
                                    assert ride != null;
                                    ProfileFragment.Companion.changeStatusWhenTimeOver(ride,activity);
                                }
                            } catch (ParseException e) {
                                Toasty.error(activity.getApplicationContext(),
                                        e.getMessage(),Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.

                    Ride Ride = dataSnapshot.getValue(Ride.class);
                    String RideKey = dataSnapshot.getKey();
                    // [START_EXCLUDE]
                    int RideIndex = RidesIds.indexOf(RideKey);
                    if (RideIndex > -1) {
                        // Replace with the new data
                        Rides.set(RideIndex, Ride);
                        // Update the RecyclerView
                        notifyItemChanged(RideIndex);
                    } else {
                        Log.w("RideAdapter", "onChildChanged:unknown_child:" + RideKey);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("RideAdapter", "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String RideKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int RideIndex = RidesIds.indexOf(RideKey);
                    if (RideIndex > -1) {
                        // Remove data from the list
                        RidesIds.remove(RideIndex);
                        Rides.remove(RideIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(RideIndex);
                    } else {
                        Log.w("RideAdapter", "onChildRemoved:unknown_child:" + RideKey);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildMoved:" + dataSnapshot.getKey());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("RideAdapter", "postRide:onCancelled", databaseError.toException());
                    // Toast.makeText(context, "Failed to load comments.",
                    //       Toast.LENGTH_SHORT).show();
                }
            };

            query.addChildEventListener(childEventListener);
            this.childEventListener = childEventListener;
        }
        public RideAdapterRecycler(final Context context, DatabaseReference reference,
                                   Activity activity, Query query, final String userId,
                                   TextView textView,String communityId) {
            this.cleanupListener();
            this.context = context;
            this.databaseReference = reference;
            this.activity = activity;
            this.Rides = new ArrayList<>();
            this.RidesIds = new ArrayList<>();
            this.userId = userId;
            this.noRidesText = textView;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault());

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildAdded " + dataSnapshot.getKey());
                        if (communityId.equals(dataSnapshot.child("community").getValue())) {
                            Ride ride = dataSnapshot.getValue(Ride.class);
                            String rideDate = null;
                            try {
                                if (ride != null) {
                                    rideDate = ride.getDate()+" "+ ride.getTime();
                                    rideDate = DateTimeAndStringHelper.dateFormat(rideDate);
                                }
                                Date dateRide = formatter.parse(rideDate);
                                Date currentDate = new Date();
                                if(dateRide.after(currentDate)){
                                    RidesIds.add(dataSnapshot.getKey());

                                    Rides.add(ride);

                                    notifyItemInserted(Rides.size() - 1);
                                }
                                else{
                                    assert ride != null;
                                    ProfileFragment.Companion.changeStatusWhenTimeOver(ride,activity);
                                }
                            } catch (ParseException e) {
                                Toasty.error(activity.getApplicationContext(),
                                        e.getMessage(),Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }


                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.

                    Ride Ride = dataSnapshot.getValue(Ride.class);
                    String RideKey = dataSnapshot.getKey();
                    // [START_EXCLUDE]
                    int RideIndex = RidesIds.indexOf(RideKey);
                    if (RideIndex > -1) {
                        // Replace with the new data
                        Rides.set(RideIndex, Ride);
                        // Update the RecyclerView
                        notifyItemChanged(RideIndex);
                    } else {
                        Log.w("RideAdapter", "onChildChanged:unknown_child:" + RideKey);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("RideAdapter", "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String RideKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int RideIndex = RidesIds.indexOf(RideKey);
                    if (RideIndex > -1) {
                        // Remove data from the list
                        RidesIds.remove(RideIndex);
                        Rides.remove(RideIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(RideIndex);
                    } else {
                        Log.w("RideAdapter", "onChildRemoved:unknown_child:" + RideKey);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                    Log.d("RideAdapter", "onChildMoved:" + dataSnapshot.getKey());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("RideAdapter", "postRide:onCancelled", databaseError.toException());
                    // Toast.makeText(context, "Failed to load comments.",
                    //       Toast.LENGTH_SHORT).show();
                }
            };

            query.addChildEventListener(childEventListener);
            this.childEventListener = childEventListener;
        }
        @NonNull
        @Override
        public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.cardview, parent, false);
            return new RideViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(@NonNull final RideViewHolder holder, int position) {
            if (Rides.isEmpty())
                this.noRidesText.setVisibility(View.VISIBLE);
            else
                this.noRidesText.setVisibility(View.GONE);
            final Ride ride = Rides.get(position);
            getUserTakeRide();
            Log.d("OnBind", ride.toString());
            DatabaseReference db = databaseReference.getDatabase().getReference().child(USERS);
            db.child(ride.getUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    String fullName;
                    if (user != null) {

                        if(user.getId().equals(userId))
                            fullName = activity.getResources().getString(R.string.you);
                        else {
                            fullName = user.getName() + " " + user.getLastName();
                            fullName = DateTimeAndStringHelper.truncate(fullName,15);
                        }
                        //Se carga la imagen sin internet
                        holder.userCard.setText(fullName);
                        ImageHelper.setImageViewPicture(holder.userPicture,activity,user.getPictureUrl());
                      /*  if (!user.getPictureUrl().isEmpty())
                            Picasso.with(activity).load(user.getPictureUrl()).fit().into(holder.userPicture);
                        else
                            Picasso.with(activity).load(R.drawable.avatar).fit().into(holder.userPicture);
*/
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toasty.error(activity.getApplicationContext(),
                            databaseError.getMessage(),Toast.LENGTH_SHORT)
                            .show();
                }
            });
            if (ride.getUser().equals(userId)) {
                holder.btnGoCard.setVisibility(View.GONE);
                holder.btnCancelCard.setVisibility(View.VISIBLE);
            }

            String originText= activity.getResources().getString(R.string.originText)
                    +" "+ DateTimeAndStringHelper.formatRoute(ride.getOrigin());

            String destinationText= activity.getResources().getString(R.string.destinationText)
                    +" "+ DateTimeAndStringHelper.formatRoute(ride.getDestination());
            holder.origin.setText(originText);
            holder.destination.setText(destinationText);
            holder.typeCard.setText(typeMessage(ride.getType()));
            holder.hour.setText(ride.getTime());
            holder.dateCard.setText(ride.getDate());
            String passengerText;

            drawRestrictions(ride.getRestrictions(),holder);
            //setPriceText(ride.getCost(),holder);
            if (ride.getType().equals(Type.OFFERED)) {
                Log.d("HERE", "YES");
                holder.rideImage.setBackground(activity.getResources().getDrawable(taxi));
                passengerText= activity.getResources().getString(R.string.seats)
                        +" "+ride.getRiders()+", "
                        +activity.getResources().getString(R.string.available)
                        +" "+numberAvailableSeats(ride);
            } else {
                passengerText=activity.getResources()
                        .getString(R.string.passengers)+" "+ ride.getRiders();
                holder.rideImage.setBackground(activity.getResources().getDrawable(como));
            }
            holder.passengerCard.setText(passengerText);
          /*  holder.frameLayoutCard.setOnLongClickListener(view -> {
           
                Intent intent = new Intent(activity, RideDetailActivity.class);
                //si el map de pasajeros no va vacio no muestra el detalle
                if(!ride.getPassengers().isEmpty()){
                    ride.getPassengers().clear();
                }
                intent.putExtra("ride", ride);
                activity.startActivity(intent);
                return false;


            });*/
            holder.frameLayoutCard.setOnClickListener((View ) -> dialog(ride).show());
            holder.btnGoCard.setOnClickListener(view -> {
                if (ride.getType().equals(Type.REQUESTED)) {
                    finishRide(RidesIds.get(holder.getAdapterPosition()));
                }
                taken(holder);
                getTokenUserForSendYourNotification(ride.getUser(), ride);
                Toasty.success(activity, activity.getResources().getString(R.string.ride_taken),
                        Toast.LENGTH_SHORT, true).show();
                addPassenger(ride);

            });
            holder.btnCancelCard.setOnClickListener(v ->
                    ProfileFragment.Companion.cancelRideDialog(ride,activity));

            disabledGoButton(userId, holder,ride);
        }

        private void addPassenger(Ride ride){
            DatabaseReference db = databaseReference.getDatabase().getReference().child(USERS);
            db.child(userId).child("activeRide").setValue(ride);
            db.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        ride.getPassengers().put(user.getId(),user);
                        databaseReference.getDatabase().getReference().child(RIDES)
                                .child(ride.getId()).child(ConstantsKt.PASSENGERS
                        ).setValue(ride.getPassengers());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return Rides.size();
        }

        private void cleanupListener() {
            if (childEventListener != null) {
                databaseReference.removeEventListener(childEventListener);
            }
        }

        private int numberAvailableSeats(Ride ride){
            int seats=ride.getRiders()-ride.getPassengers().size();
            if(seats==0){
                finishRide(ride.getId());
            }
            return seats;
        }

        private void finishRide(String key) {
            databaseReference.child(key).child(STATUS).setValue(Status.FINISHED);
        }

        private void getTokenUserForSendYourNotification(String user, final Ride ride) {
            DatabaseReference db = databaseReference.getDatabase().getReference().child(USERS);

            db.child(user).child(TOKEN).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String token = dataSnapshot.getValue(String.class);
                    if (ride.getType().equals(Type.OFFERED)) {
                        NotificationHelper.message(com.ridelineTeam.application.rideline.MainActivity.Companion.getFmc()
                                , token,  activity.getResources().getString(R.string.ride_taken),
                                activity.getResources().getString(R.string.ride_taken_body) +" "+ fullName);
                    } else {
                        NotificationHelper.message(com.ridelineTeam.application.rideline.MainActivity.Companion.getFmc()
                                , token, activity.getResources().getString(R.string.ride_accepted),
                                activity.getResources().getString(R.string.ride_accepted_body) +" "+ fullName);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        private String typeMessage(Type type){
            if(type==Type.REQUESTED){
                return  activity.getResources().getString(R.string.radioTypeRequest);
            }
            else{
                return  activity.getResources().getString(R.string.radioTypeOffer);
            }
        }
        private void taken(RideViewHolder holder) {
            DatabaseReference db = databaseReference.getDatabase().getReference().child(USERS);
            db.child(userId).child("taked")
                    .setValue(1)
                    .addOnCompleteListener(task -> holder.btnGoCard.setEnabled(false));
        }
        private void disabledGoButton(String userId, RideViewHolder holder, Ride ride) {
            DatabaseReference db = databaseReference.getDatabase().getReference().child(USERS);
            db.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        if (user.getTaked() == 1 && user.getActiveRide()!=null) {
                            holder.btnGoCard.setVisibility(View.GONE);
                            if (user.getActiveRide().getId().equals(ride.getId())){
                                holder.btnCancelCard.setVisibility(View.VISIBLE);
                            }
                        }
                        else{
                            holder.btnGoCard.setVisibility(View.VISIBLE);
                            holder.btnCancelCard.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        private void getUserTakeRide(){
            DatabaseReference db = databaseReference.getDatabase().getReference().child(USERS);
            db.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        fullName=user.getName() + " " + user.getLastName();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        private void setPriceText(RideCost rideCost,RideViewHolder holder){
           if(rideCost.getName().equals(Cost.FREE.toString())){
               holder.rideCost.setText(String.format("%s:%s", activity.getString(R.string.custom_price), activity.getString(R.string.free_ride)));
           }else if(rideCost.getName().equals(Cost.PAID.toString())){
               holder.rideCost.setText(String.format("%d", Math.round(rideCost.getPrice())));
           }
        }

        private void drawRestrictions(ArrayList<Restrictions> restrictions,RideViewHolder holder){
            for (Restrictions restriction : restrictions){
                switch (restriction){
                    case SMOKE:
                        holder.smokeIcon.setIconEnabled(false);
                        break;
                    case PET:
                        holder.petIcon.setIconEnabled(false);
                        break;
                    case FOOD:
                        holder.foodIcon.setIconEnabled(false);
                        break;
                    case CHILD:
                        holder.childIcon.setIconEnabled(false);
                        break;
                    case SLEEP:
                        holder.sleepIcon.setIconEnabled(false);
                        break;
                }
            }

        }
        private Dialog dialog(Ride ride){
            Dialog settingsDialog = new Dialog(activity);
            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            settingsDialog.setContentView(activity.getLayoutInflater().inflate(R.layout.card_click_dialog
                    , null));
           Button btnDetails= settingsDialog.findViewById(R.id.btn_details);
           TextView txtType=settingsDialog.findViewById(R.id.ride_dialog_type);
           ImageView image=settingsDialog.findViewById(R.id.circleImageViewDialog);
           if(ride.getType().equals(Type.OFFERED)){
               image.setImageResource(ic_car_3d);
               txtType.setText(activity.getString(R.string.radioTypeOffer));
           }else if(ride.getType().equals(Type.REQUESTED)){
               image.setImageResource(ic_person_location);
               txtType.setText(activity.getString(R.string.radioTypeRequest));
           }
            btnDetails.setOnClickListener((View)->{
                Intent intent = new Intent(activity, RideDetailActivity.class);
                //si el map de pasajeros no va vacio no muestra el detalle
                if(!ride.getPassengers().isEmpty()){
                    ride.getPassengers().clear();
                }
                intent.putExtra("ride", ride);
                activity.startActivity(intent);
                settingsDialog.dismiss();
            });
            return settingsDialog;
        }


    }
}
