package com.example.outfitowl;

import static com.example.outfitowl.signUp.encodeEmail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class allOutfits extends AppCompatActivity {
    private TextView close;
    private RecyclerView recyclerView;
    private ArrayList<outfitData> dataList;
    private outfitsRecyclerAdapter adapter;
    private String userEmail = null;
    private String userName = null;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("wardrobe");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_outfits);

        close = findViewById(R.id.closeButton);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
        adapter = new outfitsRecyclerAdapter(this, dataList);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userEmail = encodeEmail(currentUser.getEmail());
        }

        if (userEmail != null) {
            getUsername(userEmail, new allOutfits.OnUsernameRetrievedListener() {
                        @Override
                        public void onUsernameRetrieved(String username) {
                            userName = username;
                            databaseReference = databaseReference.child(userName).child("outfits");
                            // ValueEventListener for the Firebase Database reference
                            databaseReference.addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    dataList.clear();
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        outfitData dataClass = dataSnapshot.getValue(outfitData.class);
                                        if (dataClass != null) {
                                            dataList.add(dataClass);
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("TAG", "Error: ", error.toException());
                                }
                            });
                        }
                    });
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(allOutfits.this, outfits.class));
            }
        });

        adapter.setOnItemClickListener(new outfitsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onDeleteButtonClick(outfitData outfit) {
                AlertDialog.Builder builder = new AlertDialog.Builder(allOutfits.this);
                View dialogViewDelete = getLayoutInflater().inflate(R.layout.dialog_delete, null);
                String key = outfit.getId();
                if (key != null) {
                    DatabaseReference childReference = databaseReference.child(key);
                    builder.setView(dialogViewDelete);
                    AlertDialog dialog = builder.create();
                    if(dialog.getWindow() != null){
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                    }
                    dialog.show();
                    dialog.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            childReference.removeValue(); // Delete the child node using the key
                            dataList.remove(outfit);
                            Toast.makeText(allOutfits.this, "Item deleted", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    dialogViewDelete.findViewById(R.id.deleteCancelButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }else {
                    Toast.makeText(allOutfits.this, "Error: Unable to delete item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEditButtonClick(outfitData outfit) {
                String key = outfit.getId();
                if (key != null) {
                    Intent intent = new Intent(allOutfits.this, outfits.class);
                    intent.putExtra("editOutfit", key);
                    startActivity(intent);
                }else {
                    Toast.makeText(allOutfits.this, "Error: Unable to edit item", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public interface OnUsernameRetrievedListener {
        void onUsernameRetrieved(String username);
    }


    private void getUsername(String email, OnUsernameRetrievedListener listener) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("user");
        Query query = usersRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    listener.onUsernameRetrieved(username);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error: ", error.toException());
            }
        });
    }
}