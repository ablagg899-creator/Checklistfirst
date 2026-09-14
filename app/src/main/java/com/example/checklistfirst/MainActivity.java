package com.example.checklistfirst;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "checklist_data";
    private static final String KEY_LISTS = "lists";

    private SharedPreferences prefs;
    private JSONArray lists;
    private Spinner listSpinner;
    private LinearLayout itemsContainer;
    private EditText itemInput;
    private ArrayAdapter<String> spinnerAdapter;
    private final List<String> listNames = new ArrayList<>();
    private int selectedList = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        loadData();
        buildUi();
        refreshLists();
    }

    private void loadData() {
        try {
            String raw = prefs.getString(KEY_LISTS, null);
            if (raw == null) {
                lists = new JSONArray();
                JSONObject list = new JSONObject();
                list.put("name", "My List");
                list.put("items", new JSONArray());
                lists.put(list);
                saveData();
            } else {
                lists = new JSONArray(raw);
                if (lists.length() == 0) {
                    JSONObject list = new JSONObject();
                    list.put("name", "My List");
                    list.put("items", new JSONArray());
                    lists.put(list);
                }
            }
        } catch (Exception e) {
            lists = new JSONArray();
            try {
                JSONObject list = new JSONObject();
                list.put("name", "My List");
                list.put("items", new JSONArray());
                lists.put(list);
            } catch (Exception ignored) { }
        }
    }

    private void saveData() {
        prefs.edit().putString(KEY_LISTS, lists.toString()).apply();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView label(String text, int size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(0xFF202124);
        return view;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(12));
        root.setBackgroundColor(0xFFF7F7FB);

        TextView title = label("Checklist First", 28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = label("Simple lists. Check things off. Done.", 14);
        subtitle.setTextColor(0xFF666666);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.bottomMargin = dp(18);
        root.addView(subtitle, subParams);

        LinearLayout listRow = new LinearLayout(this);
        listRow.setGravity(Gravity.CENTER_VERTICAL);

        listSpinner = new Spinner(this);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listNames);
        listSpinner.setAdapter(spinnerAdapter);
        listSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedList = position;
                renderItems();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        listRow.addView(listSpinner, new LinearLayout.LayoutParams(0, dp(52), 1));

        Button newList = new Button(this);
        newList.setText("+ List");
        newList.setOnClickListener(v -> showNewListDialog());
        listRow.addView(newList, new LinearLayout.LayoutParams(dp(88), dp(52)));

        root.addView(listRow);

        Button deleteList = new Button(this);
        deleteList.setText("Delete current list");
        deleteList.setOnClickListener(v -> confirmDeleteList());
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(-1, dp(44));
        deleteParams.bottomMargin = dp(10);
        root.addView(deleteList, deleteParams);

        LinearLayout addRow = new LinearLayout(this);
        addRow.setGravity(Gravity.CENTER_VERTICAL);
        itemInput = new EditText(this);
        itemInput.setHint("Add something to this list");
        itemInput.setSingleLine(true);
        itemInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        addRow.addView(itemInput, new LinearLayout.LayoutParams(0, dp(56), 1));

        Button add = new Button(this);
        add.setText("Add");
        add.setOnClickListener(v -> addItem());
        addRow.addView(add, new LinearLayout.LayoutParams(dp(82), dp(56)));
        root.addView(addRow);

        TextView hint = label("Tap a checkbox to mark an item complete. Long-press an item to delete it.", 12);
        hint.setTextColor(0xFF777777);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(-1, -2);
        hintParams.topMargin = dp(4);
        hintParams.bottomMargin = dp(10);
        root.addView(hint, hintParams);

        itemsContainer = new LinearLayout(this);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(itemsContainer, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
    }

    private void refreshLists() {
        listNames.clear();
        for (int i = 0; i < lists.length(); i++) {
            try { listNames.add(lists.getJSONObject(i).getString("name")); }
            catch (Exception ignored) { listNames.add("List " + (i + 1)); }
        }
        if (selectedList >= listNames.size()) selectedList = Math.max(0, listNames.size() - 1);
        spinnerAdapter.notifyDataSetChanged();
        listSpinner.setSelection(selectedList);
        renderItems();
    }

    private void renderItems() {
        if (itemsContainer == null) return;
        itemsContainer.removeAllViews();
        if (lists.length() == 0) return;
        try {
            JSONArray items = lists.getJSONObject(selectedList).getJSONArray("items");
            int completed = 0;
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                if (item.optBoolean("checked", false)) completed++;

                CheckBox box = new CheckBox(this);
                box.setText(item.optString("text", ""));
                box.setTextSize(17);
                box.setPadding(dp(8), dp(8), dp(8), dp(8));
                box.setChecked(item.optBoolean("checked", false));
                final int index = i;
                box.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    try {
                        items.getJSONObject(index).put("checked", isChecked);
                        saveData();
                    } catch (Exception ignored) { }
                });
                box.setOnLongClickListener(v -> {
                    deleteItem(index);
                    return true;
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
                params.bottomMargin = dp(4);
                itemsContainer.addView(box, params);
            }

            if (items.length() == 0) {
                TextView empty = label("Your list is empty. Add your first item above.", 15);
                empty.setGravity(Gravity.CENTER);
                empty.setTextColor(0xFF888888);
                itemsContainer.addView(empty, new LinearLayout.LayoutParams(-1, dp(100)));
            } else {
                TextView progress = label(completed + " of " + items.length() + " completed", 13);
                progress.setTextColor(0xFF666666);
                progress.setPadding(dp(8), dp(12), dp(8), dp(4));
                itemsContainer.addView(progress, 0);
            }
        } catch (Exception ignored) { }
    }

    private void addItem() {
        String text = itemInput.getText().toString().trim();
        if (text.isEmpty()) return;
        try {
            JSONArray items = lists.getJSONObject(selectedList).getJSONArray("items");
            JSONObject item = new JSONObject();
            item.put("text", text);
            item.put("checked", false);
            items.put(item);
            saveData();
            itemInput.setText("");
            renderItems();
        } catch (Exception e) {
            Toast.makeText(this, "Could not add item", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteItem(int index) {
        try {
            JSONArray oldItems = lists.getJSONObject(selectedList).getJSONArray("items");
            JSONArray newItems = new JSONArray();
            for (int i = 0; i < oldItems.length(); i++) if (i != index) newItems.put(oldItems.get(i));
            lists.getJSONObject(selectedList).put("items", newItems);
            saveData();
            renderItems();
        } catch (Exception ignored) { }
    }

    private void showNewListDialog() {
        EditText input = new EditText(this);
        input.setHint("List name");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        new AlertDialog.Builder(this)
                .setTitle("Create a list")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    try {
                        JSONObject list = new JSONObject();
                        list.put("name", name);
                        list.put("items", new JSONArray());
                        lists.put(list);
                        selectedList = lists.length() - 1;
                        saveData();
                        refreshLists();
                    } catch (Exception ignored) { }
                }).show();
    }

    private void confirmDeleteList() {
        if (lists.length() <= 1) {
            Toast.makeText(this, "Keep at least one list.", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = listNames.get(selectedList);
        new AlertDialog.Builder(this)
                .setTitle("Delete list?")
                .setMessage("Delete \"" + name + "\" and all of its items?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        JSONArray remaining = new JSONArray();
                        for (int i = 0; i < lists.length(); i++) if (i != selectedList) remaining.put(lists.get(i));
                        lists = remaining;
                        selectedList = Math.min(selectedList, lists.length() - 1);
                        saveData();
                        refreshLists();
                    } catch (Exception ignored) { }
                }).show();
    }
}
