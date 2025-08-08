"use client";
import React, { useState } from "react";

export default function OCRUpload() {
  const [image, setImage] = useState<File | null>(null);
  const [text, setText] = useState("");

  const handleUpload = async () => {
    if (!image) return;
  
    const formData = new FormData();
    formData.append("file", image);
  
    const res = await fetch("/api/ocr", {
      method: "POST",
      body: formData,
    });
  
    const data = await res.json();
    setText(data.text);
  };

  return (
    <div>
      <input
        type="file"
        accept="image/*,.pdf" // ← pdf도 허용
        onChange={(e) => setImage(e.target.files?.[0] || null)}
      />
      <button onClick={handleUpload}>OCR 추출</button>
      <pre>{text}</pre>
    </div>
  );
}

function toBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve((reader.result as string).split(",")[1]);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}
