<?php
require_once 'config.php';
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    if (!empty($data['name'])) {
        $sql = "INSERT INTO scores (name, avatar, time, difficulty, points, win)
                VALUES (?, ?, ?, ?, ?, ?)";

        $stmt = $pdo->prepare($sql);
        $stmt->execute([
            $data['name'],
            $data['avatar'] ?? '👦',
            $data['time'] ?? 0,
            $data['difficulty'] ?? 'Fácil',
            $data['points'] ?? 0,
            ($data['win'] ?? false) ? 1 : 0
        ]);

        echo json_encode(['status' => 'success', 'id' => $pdo->lastInsertId()]);
    } else {
        header('', true, 400);
        echo json_encode(['status' => 'error', 'message' => 'Nome é obrigatório']);
    }
}
?>