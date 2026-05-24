<?php
require_once 'config.php';
header('Content-Type: application/json');

$sql = "SELECT name, avatar, time, difficulty, points, win FROM scores ORDER BY points DESC LIMIT 50";
$stmt = $pdo->query($sql);
$results = $stmt->fetchAll();

// Converter o campo 'win' de 0/1 (int) para true/false (bool) para o Android
foreach ($results as &$row) {
    $row['win'] = (bool)$row['win'];
    // Garantir que campos numéricos sejam inteiros de fato
    $row['time'] = (int)$row['time'];
    $row['points'] = (int)$row['points'];
}

echo json_encode($results);
?>