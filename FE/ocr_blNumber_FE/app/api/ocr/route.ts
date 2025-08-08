import { NextResponse } from "next/server";

export async function POST(req: Request) {
  try {
    const { image } = await req.json();
    console.log("Received image data. Length:", image.length);

    // Java 백엔드 URL (개발 환경 기준)
    const backendUrl = process.env.BACKEND_URL || "http://localhost:8080";

    // Base64 이미지 데이터를 FormData로 변환
    const formData = new FormData();

    // Base64를 Blob으로 변환
    const base64Data = image.replace(/^data:image\/[a-z]+;base64,/, "");
    const byteCharacters = atob(base64Data);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: "image/png" });

    formData.append("file", blob, "image.png");

    // Java 백엔드로 요청 전송
    const response = await fetch(`${backendUrl}/api/ocr`, {
      method: "POST",
      body: formData,
    });

    console.log("Status from Java backend:", response.status);

    if (!response.ok) {
      throw new Error(`Backend responded with status: ${response.status}`);
    }

    const result = await response.json();
    console.log("Result from Java backend:", result);

    return NextResponse.json({ text: result.text });
  } catch (error) {
    console.error("An error occurred:", error);
    return NextResponse.json(
      { error: "Internal Server Error" },
      { status: 500 }
    );
  }
}
