package com.sirajul.gitpuaher.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sirajul.gitpuaher.R;
import com.sirajul.gitpuaher.models.FileItem;
import com.sirajul.gitpuaher.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileViewHolder> {

    private Context context;
    private List<FileItem> fileList;

    public FileListAdapter(Context context) {
        this.context = context;
        this.fileList = new ArrayList<>();
    }

    public void setFileList(List<FileItem> fileList) {
        this.fileList = fileList != null ? fileList : new ArrayList<FileItem>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_item, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        if (fileList != null && position < fileList.size()) {
            FileItem fileItem = fileList.get(position);
            holder.bind(fileItem);
        }
    }

    @Override
    public int getItemCount() {
        return fileList != null ? fileList.size() : 0;
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFileIcon;
        private TextView tvFileName, tvFilePath, tvFileSize;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFileIcon = itemView.findViewById(R.id.iv_file_icon);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFilePath = itemView.findViewById(R.id.tv_file_path);
            tvFileSize = itemView.findViewById(R.id.tv_file_size);
        }

        public void bind(FileItem fileItem) {
            if (fileItem == null) return;

            tvFileName.setText(fileItem.getName() != null ? fileItem.getName() : "Unknown");
            
            // Show relative path
            String displayPath = fileItem.getPath() != null ? fileItem.getPath() : "";
            if (displayPath.length() > 50) {
                displayPath = "..." + displayPath.substring(displayPath.length() - 47);
            }
            tvFilePath.setText(displayPath);

            // Set icon based on file type
            if (fileItem.isDirectory()) {
                ivFileIcon.setImageResource(R.drawable.ic_folder);
                tvFileSize.setText("Folder");
            } else {
                ivFileIcon.setImageResource(R.drawable.ic_file);
                String sizeText = FileUtils.getReadableFileSize(fileItem.getSize());
                tvFileSize.setText(sizeText != null ? sizeText : "0 B");
            }
        }
    }
}