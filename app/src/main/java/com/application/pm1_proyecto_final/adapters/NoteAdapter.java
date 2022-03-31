package com.application.pm1_proyecto_final.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.application.pm1_proyecto_final.R;
import com.application.pm1_proyecto_final.listeners.Notelistener;
import com.application.pm1_proyecto_final.models.Note;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{
    private  List<Note> listNote;
    private Notelistener notelistener;
    private  List<Note> filterlist;
    private  CustomFilter filter;
    public NoteAdapter(List<Note> listNote,Notelistener notelistener) {
        this.listNote = listNote;
        this.notelistener = notelistener;
        this.filterlist = listNote;
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
        TextView title, description,date;
        ConstraintLayout card;
        View view;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textTitleNote);
            description = itemView.findViewById(R.id.textDescriptionNote);
            date = itemView.findViewById(R.id.textDateNote);
            card = itemView.findViewById(R.id.cardNote);

            view = itemView;

        }

        void setData(Note note)  {

            title.setText(note.getTitle());

            description.setText(note.getDescription());

            date.setText(note.getDate());

            view.setOnClickListener(v -> notelistener.onClickNote(note));
        }


    }
    //Filter
    /**********************************************************************************/

    class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults filterResults = new FilterResults();

            if(charSequence != null && charSequence.length()>0){

                charSequence = charSequence.toString().toUpperCase();

                ArrayList<Note> filters = new ArrayList<Note>();

                for(int i = 0;i < filterlist.size(); i++){

                    if(filterlist.get(i).getTitle().toUpperCase().contains(charSequence)||
                            filterlist.get(i).getDescription().toUpperCase().contains(charSequence)){

                        filters.add(filterlist.get(i));
                    }
                }

                filterResults.count = filters.size();
                filterResults.values = filters;

            }else {

                filterResults.count = filterlist.size();
                filterResults.values = filterlist;
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            listNote = (ArrayList<Note>) filterResults.values;
            notifyDataSetChanged();
        }
    }

    public Filter getFilter(){

        if(filter == null){
            filter = new CustomFilter();
        }

        return filter;
    }

    public ArrayList<Note> getFilterlist(){
        return (ArrayList<Note>) filterlist;
    }
    

}
