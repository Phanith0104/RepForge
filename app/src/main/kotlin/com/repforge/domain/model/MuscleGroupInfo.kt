package com.repforge.domain.model

data class MuscleGroupInfo(
    val group: MuscleGroup,
    val parts: List<String>
)

val MUSCLE_GROUPS_INFO = listOf(
    MuscleGroupInfo(MuscleGroup.CHEST, listOf("Upper Chest", "Middle Chest", "Lower Chest")),
    MuscleGroupInfo(MuscleGroup.BACK, listOf("Upper Back", "Middle Back", "Lower Back", "Lats")),
    MuscleGroupInfo(MuscleGroup.SHOULDERS, listOf("Front Deltoid", "Side Deltoid", "Rear Deltoid")),
    MuscleGroupInfo(MuscleGroup.BICEPS, listOf("Long Head", "Short Head", "Brachialis", "Brachioradialis")),
    MuscleGroupInfo(MuscleGroup.TRICEPS, listOf("Long Head", "Lateral Head", "Medial Head")),
    MuscleGroupInfo(MuscleGroup.LEGS, listOf("Rectus Femoris", "Vastus Lateralis", "Vastus Medialis", "Hamstrings", "Gastrocnemius", "Soleus", "Glutes")),
    MuscleGroupInfo(MuscleGroup.FOREARMS, listOf("Flexors", "Extensors", "Brachioradialis", "Pronators", "Supinator")),
    MuscleGroupInfo(MuscleGroup.ABS, listOf("Upper Abs", "Middle Abs", "Lower Abs", "External Obliques", "Internal Obliques"))
)
