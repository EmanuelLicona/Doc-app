package com.application.pm1_proyecto_final.adapters;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.activities.EditActivityNote;
import com.application.pm1_proyecto_final.listeners.Notelistener;
import com.application.pm1_proyecto_final.models.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{
    private  List<Note> listNote;
    private Notelistener notelistener;
    public NoteAdapter(List<Note> listNote,Notelistener notelistener) {
        this.listNote = listNote;
        this.notelistener = notelistener;
    }

    @NonNull
    @Override
    public NoteAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_container_note, parent, false);

        return new NoteAdapter.NoteViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NoteViewHolder holder, int position) {
        holder.setData(listNote.get(position));


    }
    @Override
    public int getItemCount() {
        return listNote.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView title, description;
        ConstraintLayout card;
        View view;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textTitleNote);
            description = itemView.findViewById(R.id.textDescriptionNote);

            card = itemView.findViewById(R.id.cardNote);

            view = itemView;

        }

        void setData(Note note){

            title.setText(note.getTitle());

            description.setText(note.getDescription());

            view.setOnClickListener(v -> notelistener.onClickNote(note));
        }


    }
}
