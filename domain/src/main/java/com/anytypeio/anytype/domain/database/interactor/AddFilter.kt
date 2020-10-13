package com.anytypeio.anytype.domain.database.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.database.FilterMock
import com.anytypeio.anytype.domain.database.model.Filter

class AddFilter : BaseUseCase<Unit, AddFilter.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Unit> = try {
        FilterMock.filters.add(params.filter)
        Either.Right(Unit)
    } catch (e: Throwable) {
        Either.Left(e)
    }

    data class Params(val filter: Filter)
}