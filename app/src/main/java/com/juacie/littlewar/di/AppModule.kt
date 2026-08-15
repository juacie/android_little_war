package com.juacie.littlewar.di

import com.juacie.littlewar.data.repository.BattleRepositoryImpl
import com.juacie.littlewar.data.repository.FormationRepositoryImpl
import com.juacie.littlewar.data.repository.GameDataRepositoryImpl
import com.juacie.littlewar.data.repository.StageRepositoryImpl
import com.juacie.littlewar.domain.repository.BattleRepository
import com.juacie.littlewar.domain.repository.FormationRepository
import com.juacie.littlewar.domain.repository.GameDataRepository
import com.juacie.littlewar.domain.repository.StageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindGameDataRepository(impl: GameDataRepositoryImpl): GameDataRepository

    @Binds
    abstract fun bindFormationRepository(impl: FormationRepositoryImpl): FormationRepository

    @Binds
    abstract fun bindStageRepository(impl: StageRepositoryImpl): StageRepository

    @Binds
    abstract fun bindBattleRepository(impl: BattleRepositoryImpl): BattleRepository
}
