package com.notes.notes.controller.taskModuleControllers;

import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.service.authServices.UserService;
import com.notes.notes.service.taskModuleServices.TaskCommentService;
import com.notes.notes.service.taskModuleServices.TaskService;
import com.notes.notes.util.FileStorageConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;



@Controller
@RequestMapping("/image")
public class ImageUploadController {

    private final TaskCommentService taskCommentService;
    private final TaskService taskService;
    private final UserService userService;

    public ImageUploadController(TaskCommentService taskCommentService,
                                 TaskService taskService,
                                 UserService userService) {
        this.taskCommentService = taskCommentService;
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/upload-page")
    public String uploadPage() {
        return "tasks/upload-image";
    }

    @PostMapping("/upload/{taskId}")
    public String uploadImage(
            @PathVariable Long taskId,
            @RequestParam("image") MultipartFile image,
            @ModelAttribute("loggedInUser") User loggedInUser,
            RedirectAttributes redirectAttributes
    ) {
        try {

            if (image.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "No file selected");
                return "redirect:/tasks/" + taskId;
            }

            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage", "Only image files are allowed");
                return "redirect:/tasks/" + taskId;
            }

            // ❗ Validate file size (5MB max)
            if (image.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage", "File size must be under 5MB");
                return "redirect:/tasks/" + taskId;
            }

            // 📁 Ensure directory exists
            Path uploadDir = Paths.get(FileStorageConfig.IMAGE_UPLOAD_DIR);
            Files.createDirectories(uploadDir);

            // 🧾 Original & stored filenames
            String originalName = image.getOriginalFilename();
            String storedName = System.currentTimeMillis() + "_" + originalName;

            Path filePath = uploadDir.resolve(storedName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 📌 Fetch task
            Task task = taskService.getTaskById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            // 💬 Save comment (FILE message)
            taskCommentService.addComment(
                    task,
                    loggedInUser,
                    null,        // no text content
                    storedName   // attachment
            );

            redirectAttributes.addFlashAttribute("successMessage", "File uploaded");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Upload failed");
        }

        return "redirect:/tasks/" + taskId;
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filename) throws IOException {

        Path filePath = Paths.get(FileStorageConfig.IMAGE_UPLOAD_DIR).resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("File not found");
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // 👇 THIS LINE enables open-in-new-tab + download
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


}
