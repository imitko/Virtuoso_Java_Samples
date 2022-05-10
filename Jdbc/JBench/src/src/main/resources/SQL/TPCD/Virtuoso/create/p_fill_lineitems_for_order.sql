--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure tpcd..fill_lineitems_for_order(in _o_orderkey integer, in _o_orderdate date, out _o_orderstatus character(1), out _o_totalprice numeric(20, 2)) {
   
	declare _l_orderkey, _l_partkey, _l_suppkey, _l_linenumber  integer;
	declare _l_returnflag, _l_linestatus, _l_shipinstruct, _l_shipmode,  _l_comment varchar;
	declare _l_quantity, _l_extendedprice, _l_discount, _l_tax varchar;
	declare _l_shipdate, _l_commitdate, _l_receiptdate date;
	
	declare numLines, suppIndex, numFs, numOs integer;
	declare _p_retailprice numeric(20, 2);
	declare currentDate date;
	
	currentDate := stringdate('1995.06.17');
	numLines := tpcd..randomNumber(1, 7);
	_l_linenumber := 1;
	_o_totalprice := 0;
	suppIndex := 0;
	numFs := 0;
	numOs := 0;
	while (_l_lineNumber <= numLines) {
		_l_orderkey := _o_orderkey;
		_l_partkey := tpcd..randomNumber(1, 200000);
		_l_suppkey := mod(_l_partkey + (mod(suppIndex, 4) * (2500 + (_l_partkey - 1)/10000)), 10001);
		_l_quantity := tpcd..randomNumeric(1, 50);
					 
		declare cr cursor for select p_retailprice from tpcd..part where p_partkey = _l_partkey;
		open cr;
		fetch cr into _p_retailprice;
		close cr;
		_l_extendedprice := _l_quantity * _p_retailprice;
		_l_discount := tpcd..randomNumeric(0.0, 0.10);
		_l_tax := tpcd..randomNumeric(0.0, 0.08);
		_l_shipdate := dateadd('day', tpcd..randomNumber(1, 121), _o_orderdate);
		_l_commitdate := dateadd('day', tpcd..randomNumber(30, 90), _o_orderdate);
		_l_receiptdate := dateadd('day', tpcd..randomNumber(1, 30), _l_shipdate);
		if (datediff('day', _l_receiptdate, currentDate) > 0) {
			if (tpcd..randomNumber(0, 1) > 0)
				_l_returnflag := 'R';
			else
				_l_returnflag := 'A';
		} else
			_l_returnflag := 'N';
		
		if (datediff('day', _l_shipdate, currentDate) > 0) {
			_l_linestatus := 'F';
			numFs := numFs + 1;
		} else {
			_l_linestatus := 'O';
			numOs := numOs + 1;
		}
		
		_l_shipinstruct := tpcd..randomInstruction(0);
		_l_shipmode := tpcd..randomMode(0);
		_l_comment := tpcd..randomString(27, 0, 1);
	
		_o_totalprice := _o_totalprice + (_l_extendedprice * (1 + _l_tax) * (1 - _l_discount));
		suppIndex := suppIndex + 1;
		
		insert into tpcd..lineitem (l_orderkey, l_partkey, l_suppkey, l_linenumber, l_quantity, l_extendedprice, l_discount, l_tax, l_returnflag, l_linestatus, l_shipdate, l_commitdate, l_receiptdate, l_shipinstruct, l_shipmode, l_comment) values (_l_orderkey, _l_partkey, _l_suppkey, _l_linenumber, _l_quantity, _l_extendedprice, _l_discount, _l_tax, _l_returnflag, _l_linestatus, _l_shipdate, _l_commitdate, _l_receiptdate, _l_shipinstruct, _l_shipmode, _l_comment);
		_l_linenumber := _l_linenumber + 1;
	}
	if (numOs > 0) {
	    if (numFs = 0)
			_o_orderstatus := 'O';
	    else
		    _o_orderstatus := 'P';
	} else {
		if (numFs = 0)
		    _o_orderstatus := 'P';
	    else
			_o_orderstatus := 'F';
	}
}
