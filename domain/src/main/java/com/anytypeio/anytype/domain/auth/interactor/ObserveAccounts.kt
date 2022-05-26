package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.FlowUseCase

class ObserveAccounts(
    private val repository: AuthRepository
) : FlowUseCase<Account, Unit>() {

    override fun build(
        params: Unit?
    ) = repository.observeAccounts()
}